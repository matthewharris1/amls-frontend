/*
 * Copyright 2020 HM Revenue & Customs
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

package models.hvd

import jto.validation.{Invalid, Path, Valid, ValidationError}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

class CashPaymentOverTenThousandEurosSpec extends PlaySpec with MockitoSugar {

  "CashPaymentOverTenThousandEurosSpec" should {

    "Form Validation" must {

      "successfully validate given a `No` value" in {
        CashPaymentOverTenThousandEuros.formRule.validate(Map("acceptedAnyPayment" -> Seq("false"))) must
          be(Valid(CashPaymentOverTenThousandEuros(false)))
      }

      "successfully validate given an `Yes` value" in {
        CashPaymentOverTenThousandEuros.formRule.validate(Map("acceptedAnyPayment" -> Seq("true"))) must
          be(Valid(CashPaymentOverTenThousandEuros(true)))
      }

      "fail to validate when neither 'Yes' nor 'No' is selected" in {
        CashPaymentOverTenThousandEuros.formRule.validate(Map.empty) must
          be(Invalid(Seq(
            (Path \ "acceptedAnyPayment") -> Seq(ValidationError("error.required.hvd.accepted.cash.payment"))
          )))
      }
    }
  }
}
