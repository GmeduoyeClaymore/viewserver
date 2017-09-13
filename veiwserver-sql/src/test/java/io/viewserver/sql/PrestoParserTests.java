/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.viewserver.sql;

import com.facebook.presto.sql.parser.SqlParser;
import com.facebook.presto.sql.tree.Query;
import org.junit.Test;

/**
 * Created by nick on 12/07/15.
 */
public class PrestoParserTests {
    @Test
    public void test() {
        String sql = "select client, day + 10 " +
                "from \"/negotiations\" n " +
//                "join currencies c on c.id = n.currencyId and c.date = n.date " +
//                "join type t on id = type " +
//                "where day=0 " +
//                "group by client " +
                "having sum(dv01) > 0 " +
//                "order by timeStamp " +
                "limit 10";

        SqlParser parser = new SqlParser();
        Query query = (Query) parser.createStatement(sql);
    }
}
