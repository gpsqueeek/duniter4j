package org.duniter.core.client.service.exception;

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


import org.duniter.core.exception.BusinessException;

public class InsufficientCreditException extends BusinessException {

	private static final long serialVersionUID = -5260280401104018980L;

	public InsufficientCreditException() {
        super();
    }

    public InsufficientCreditException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientCreditException(String message) {
        super(message);
    }

    public InsufficientCreditException(Throwable cause) {
        super(cause);
    }
	
}
