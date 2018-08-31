/*
 * Copyright 2018 HM Revenue & Customs
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

package views.notifications.v1

import models.notifications.NotificationParams
import org.scalatest.MustMatchers
import utils.AmlsSpec
import views.Fixture

class minded_to_revoke_flattenSpec extends AmlsSpec with MustMatchers {

    trait ViewFixture extends Fixture {
        implicit val requestWithToken = addToken(request)
        val notificationParams = NotificationParams("msgTitle", "msgContent", businessName = "businessName")
    }

    "minded_to_revoke flattened view" must {
        "be the same as non-flattened view" in new ViewFixture {
            val viewV1 = views.html.notifications.v1.minded_to_revoke(notificationParams)
            val htmlV1 = viewV1.body.filterNot(Set('\n', '\t', ' ').contains)

            val view = views.html.notifications.minded_to_revoke("msgTitle", "msgContent", "businessName")
            val htmlUnflattened = view.body.filterNot(Set('\n', '\t', ' ').contains)

            htmlV1 mustEqual htmlUnflattened
        }
    }

}
