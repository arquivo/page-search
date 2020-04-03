/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.arquivo.indexer.utils;

/*-
 * Copyright (C) 2013 - 2020 The webarchive-discovery project contributors
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 */

import org.apache.commons.httpclient.Header;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Thin wrapper for ArrayList holding HTTP headers and HTTP status.
 */
public class HTTPHeader extends ArrayList<Header> {
    private String httpStatus;
    public HTTPHeader(int initialCapacity) {
        super(initialCapacity);
    }

    public HTTPHeader() {
    }

    public HTTPHeader(Collection<? extends Header> c) {
        super(c);
    }

    public String getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
    }

    public void addAll(Header[] httpHeaders) {
        addAll(Arrays.asList(httpHeaders));
    }
    
    public String getHeader(String key, String defaultValue) {
        key = key.toLowerCase();
        for (Header header: this) {
            if (key.equals(header.getName().toLowerCase())) {
                return header.getValue();
            }
        }
        return defaultValue;
    }
}
