package org.duniter.elasticsearch.cli;

/*
 * #%L
 * UCoin Java Client :: Core API
 * %%
 * Copyright (C) 2014 - 2015 EIS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.google.common.collect.Lists;
import org.duniter.core.exception.TechnicalException;
import org.duniter.core.util.CommandLinesUtils;
import org.duniter.core.util.StringUtils;
import org.duniter.elasticsearch.config.Configuration;
import org.duniter.elasticsearch.config.ConfigurationAction;
import org.duniter.elasticsearch.service.ServiceLocator;
import org.duniter.elasticsearch.util.Desktop;
import org.duniter.elasticsearch.util.DesktopPower;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.nuiton.config.ApplicationConfig;
import org.nuiton.i18n.I18n;
import org.nuiton.i18n.init.DefaultI18nInitializer;
import org.nuiton.i18n.init.UserI18nInitializer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Main {

    private static String TITLE_SEPARATOR_LINE = "************************************************\n";
    private static String TITLE_EMPTY_LINE = "*\n";
    private static String TITLE = TITLE_SEPARATOR_LINE
            + TITLE_EMPTY_LINE
            + "* %s\n" // title
            + TITLE_EMPTY_LINE
            + "* %s\n" // sub-title
            + TITLE_EMPTY_LINE + TITLE_SEPARATOR_LINE;

    private static final ESLogger log = ESLoggerFactory.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Main main = new Main();
        main.run(args);
    }

    public void run(String[] args) {
        if (log.isInfoEnabled()) {
            log.info("Starting duniter4j :: ElasticSearch Indexer with arguments " + Arrays.toString(args));
        }

        // By default, start
        if (args == null || args.length == 0) {
            args = new String[] { "--start" };
        }

        List<String> arguments = Lists.newArrayList(Arrays.asList(args));
        arguments.removeAll(Arrays.asList(ConfigurationAction.HELP.aliases));

        // Could override config file name (useful for dev)
        String configFile = "duniter4j-elasticsearch.config";
        if (System.getProperty(configFile) != null) {
            configFile = System.getProperty(configFile);
            configFile = configFile.replaceAll("\\\\", "/");
        }
        
        // Create configuration
        Configuration config = new Configuration(configFile, args) {
            protected void addAlias(ApplicationConfig applicationConfig) {
                super.addAlias(applicationConfig);
                // Add custom alias
            };
        };
        Configuration.setInstance(config);

        // Init i18n
        try {
            initI18n(config);
        } catch (IOException e) {
            throw new TechnicalException("i18n initialization failed", e);
        }

        // Add hook on system
        addShutdownHook();

        // Run all actions
        try {
            config.getApplicationConfig().doAllAction();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (config.isDaemon()) {
            while (true) {
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException e) {
                  // Nothing
                }
            }
        }
        else {
            if (arguments.size() > 0) {

                // Check if auto-quit if need
                boolean quit = true;
                for (String startAlias : ConfigurationAction.START.aliases) {
                    if (arguments.contains(startAlias)) {
                        quit = false;
                        break;
                    }
                }

                // If scheduling is running, wait quit instruction
                if (!quit) {

                    while (!quit) {
                        String userInput = CommandLinesUtils.readInput(
                                String.format(TITLE,
                                        "duniter4j :: Elasticsearch successfully started",
                                        ">> To quit, press [Q] or [enter]"),
                                "Q", true);
                        quit = StringUtils.isNotBlank(userInput) && "Q".equalsIgnoreCase(userInput);
                    }
                }
            }
        }

        // shutdown
        shutdown();
    }

    /* -- protected methods -- */

    /**
     * Shutdown all services
     */
    protected static void shutdown() {
        if (ServiceLocator.instance() != null) {
            try {
                ServiceLocator.instance().close();
            }
            catch(IOException e) {
                // Silent is gold
            }
        }

        log.info("duniter4j :: ElasticSearch Indexer successfully stopped");
    }

    protected void initI18n(Configuration config) throws IOException {

        // --------------------------------------------------------------------//
        // init i18n
        // --------------------------------------------------------------------//
        File i18nDirectory = new File(config.getDataDirectory(), "i18n");
        if (i18nDirectory.exists()) {
            // clean i18n cache
            FileUtils.cleanDirectory(i18nDirectory);
        }

        FileUtils.forceMkdir(i18nDirectory);

        if (log.isDebugEnabled()) {
            log.debug("I18N directory: " + i18nDirectory);
        }

        Locale i18nLocale = config.getI18nLocale();

        if (log.isInfoEnabled()) {
            log.info(String.format("Starts i18n with locale [%s] at [%s]",
                    i18nLocale, i18nDirectory));
        }
        I18n.init(new UserI18nInitializer(
                i18nDirectory, new DefaultI18nInitializer(getI18nBundleName())),
                i18nLocale);
    }

    protected String getI18nBundleName() {
        return "duniter4j-elasticsearch-i18n";
    }

    /**
     * Add an OS shutdown hook, to close application on shutdown
     */
    private void addShutdownHook() {

        // Use shutdownHook to close context on System.exit
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                shutdown();
            }
        }));

        // Add DesktopPower to hook computer shutdown
        DesktopPower desktopPower = Desktop.getDesktopPower();
        if (desktopPower != null) {

            desktopPower.addListener(new DesktopPower.Listener() {
                @Override
                public void quit() {
                   if (ServiceLocator.instance() != null) {
                       try {
                           ServiceLocator.instance().close();
                       }
                       catch(IOException e) {
                           // Silent is gold
                       }
                   }
                }
            });
        }

    }
}