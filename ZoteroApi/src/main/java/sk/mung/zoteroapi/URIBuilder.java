/* ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package sk.mung.zoteroapi;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;

/**
 * {@link URI} builder for HTTP requests.
 *
 * @since 4.2
 */
@NotThreadSafe
public class URIBuilder
{
    private String scheme;
    private String encodedSchemeSpecificPart;
    private String encodedAuthority;
    private String userInfo;
    private String encodedUserInfo;
    private String host;
    private int port;
    private String path;
    private String encodedPath;
    private String encodedQuery;
    private List<NameValuePair> queryParams;
    private String fragment;
    private String encodedFragment;

    /**
     * Construct an instance from the string which must be a valid URI.
     *
     * @param string a valid URI in string form
     * @throws URISyntaxException if the input is not a valid URI
     */
    public URIBuilder(final String string) throws URISyntaxException
    {
        super();
        digestURI(new URI(string));
    }

    private List<NameValuePair> parseQuery(final URI uri, final Charset charset)
    {
        if (uri != null )
        {
            return new ArrayList<NameValuePair>(URLEncodedUtils.parse(uri, charset.name()));
        }
        return null;
    }

    /**
     * Builds a {@link URI} instance.
     */
    public URI build() throws URISyntaxException
    {
        return new URI(buildString());
    }

    private String buildString()
    {
        StringBuilder sb = new StringBuilder();
        if (this.scheme != null)
        {
            sb.append(this.scheme).append(':');
        }
        if (this.encodedSchemeSpecificPart != null)
        {
            sb.append(this.encodedSchemeSpecificPart);
        } else
        {
            if (this.encodedAuthority != null)
            {
                sb.append("//").append(this.encodedAuthority);
            } else if (this.host != null)
            {
                sb.append("//");
                if (this.encodedUserInfo != null)
                {
                    sb.append(this.encodedUserInfo).append("@");
                } else if (this.userInfo != null)
                {
                    sb.append(encodeUserInfo(this.userInfo)).append("@");
                }
                if (InetAddressUtils.isIPv6Address(this.host))
                {
                    sb.append("[").append(this.host).append("]");
                } else
                {
                    sb.append(this.host);
                }
                if (this.port >= 0)
                {
                    sb.append(":").append(this.port);
                }
            }
            if (this.encodedPath != null)
            {
                sb.append(normalizePath(this.encodedPath));
            } else if (this.path != null)
            {
                sb.append(encodePath(normalizePath(this.path)));
            }
            if (this.encodedQuery != null)
            {
                sb.append("?").append(this.encodedQuery);
            } else if (this.queryParams != null)
            {
                sb.append("?").append(encodeQuery(this.queryParams));
            }
        }
        if (this.encodedFragment != null)
        {
            sb.append("#").append(this.encodedFragment);
        } else if (this.fragment != null)
        {
            sb.append("#").append(encodeFragment(this.fragment));
        }
        return sb.toString();
    }

    private void digestURI(final URI uri)
    {
        this.scheme = uri.getScheme();
        this.encodedSchemeSpecificPart = uri.getRawSchemeSpecificPart();
        this.encodedAuthority = uri.getRawAuthority();
        this.host = uri.getHost();
        this.port = uri.getPort();
        this.encodedUserInfo = uri.getRawUserInfo();
        this.userInfo = uri.getUserInfo();
        this.encodedPath = uri.getRawPath();
        this.path = uri.getPath();
        this.encodedQuery = uri.getRawQuery();
        this.queryParams = parseQuery(uri, Consts.UTF_8);
        this.encodedFragment = uri.getRawFragment();
        this.fragment = uri.getFragment();
    }

    private String encodeUserInfo(final String userInfo)
    {

        try
        {
            return URLEncoder.encode(userInfo, Consts.UTF_8.name());
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String encodePath(final String path)
    {
        try
        {
            return URLEncoder.encode(path, Consts.UTF_8.name());
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String encodeQuery(final List<NameValuePair> params)
    {
        return URLEncodedUtils.format(params, Consts.UTF_8.name());
    }

    private String encodeFragment(final String fragment)
    {
        try
        {
            return URLEncoder.encode(fragment, Consts.UTF_8.name());
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }


    /**
     * Adds parameter to URI query. The parameter name and value are expected to be unescaped
     * and may contain non ASCII characters.
     */
    public URIBuilder addParameter(final String param, final String value)
    {
        if (this.queryParams == null)
        {
            this.queryParams = new ArrayList<NameValuePair>();
        }
        this.queryParams.add(new BasicNameValuePair(param, value));
        this.encodedQuery = null;
        this.encodedSchemeSpecificPart = null;
        return this;
    }

    public List<NameValuePair> getQueryParams()
    {
        if (this.queryParams != null)
        {
            return new ArrayList<NameValuePair>(this.queryParams);
        } else
        {
            return new ArrayList<NameValuePair>();
        }
    }

    @Override
    public String toString()
    {
        return buildString();
    }

    private static String normalizePath(String path)
    {
        if (path == null)
        {
            return null;
        }
        int n = 0;
        for (; n < path.length(); n++)
        {
            if (path.charAt(n) != '/')
            {
                break;
            }
        }
        if (n > 1)
        {
            path = path.substring(n - 1);
        }
        return path;
    }

    public String getQueryParameter(@NotNull String name)
    {
        for(NameValuePair param : getQueryParams())
        {
            if(name.equals(param.getName()))
                return param.getValue();
        }
        return null;
    }
}