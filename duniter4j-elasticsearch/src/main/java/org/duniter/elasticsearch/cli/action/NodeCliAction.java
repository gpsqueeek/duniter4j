package org.duniter.elasticsearch.cli.action;

/*
 * #%L
 * SIH-Adagio :: Shared
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2012 - 2014 Ifremer
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

import org.duniter.elasticsearch.config.Configuration;
import org.duniter.elasticsearch.service.ElasticSearchService;
import org.duniter.elasticsearch.service.ServiceLocator;
import org.duniter.elasticsearch.service.market.MarketCategoryIndexerService;
import org.duniter.elasticsearch.service.market.MarketRecordIndexerService;
import org.duniter.elasticsearch.service.registry.RegistryCurrencyIndexerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeCliAction {
	/* Logger */
	private static final Logger log = LoggerFactory.getLogger(NodeCliAction.class);

	public void start() {

        Configuration config = Configuration.instance();
        //config.setNodeElasticSearchLocal(false);

        // Starting ES node
        ElasticSearchService esService = ServiceLocator.instance().getElasticSearchService();
        esService.startNode();

        // Wait 5s, to avoid error on existsIndex()
        try {
            Thread t = new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        sleep(5000);
                    }
                    catch(InterruptedException e) {
                        // continue
                    }
                    catch(IllegalMonitorStateException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
            t.join();
        }
        catch(InterruptedException e) {
            // continue
        }


        // Create indexed if need
        {
            // Currency index
            RegistryCurrencyIndexerService currencyIndexerService = ServiceLocator.instance().getRegistryCurrencyIndexerService();
            currencyIndexerService.createIndexIfNotExists();

            // Product index
            MarketRecordIndexerService recordIndexerService = ServiceLocator.instance().getMarketRecordIndexerService();
            recordIndexerService.createIndexIfNotExists();

            // Category index
            MarketCategoryIndexerService categoryIndexerService = ServiceLocator.instance().getMarketCategoryIndexerService();
            categoryIndexerService.createIndexIfNotExists();
        }
	}

    /*public void stop() {
        // Starting ES node
        ElasticSearchService esService = ServiceLocator.instance().getElasticSearchService();
        esService.stopNode();
    }*/

    /* -- protected methods -- */

}