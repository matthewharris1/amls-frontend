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

package utils

import forms.EmptyForm
import models.businessmatching.updateservice.{ResponsiblePeopleFitAndProper, TradingPremisesActivities}
import models.businessmatching._
import models.flowmanagement.AddBusinessTypeFlowModel
import models.responsiblepeople.{PersonName, ResponsiblePerson}
import models.tradingpremises.{Address, TradingPremises, YourTradingPremises}
import org.joda.time.LocalDate
import views.Fixture
import views.html.businessmatching.updateservice.add._

/**
  * Trait to hold the fixtures used for data setup to mixin with the UpdateServicesSummarySpec class.
  *
  * Holds traits required to test the check your answers page given FX.
  */
trait UpdateServicesSummaryFixtures  extends AmlsSpec {

  /**
    * ViewFixture.
    *
    * Extends Fixture and handles oAuth.
    * All other traits will extend to inherit oAuth
    */
  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)
  }

  /**
    * SimpleFlowModelViewFixture.
    *
    * View in simple form with high value dealing.
    */
  trait SimpleFlowModelViewFixture extends ViewFixture {
    override def view = update_services_summary(EmptyForm, AddBusinessTypeFlowModel(
      activity = Some(HighValueDealing),
      areNewActivitiesAtTradingPremises = Some(true)
    ), Seq(), Seq())
  }

  /**
    * SimpleTCSPViewFixture.
    *
    * View to include TrustAndCompanyServices.
    * Also includes fit and proper and has a responsible person.
    */
  trait SimpleTCSPViewFixture extends ViewFixture {
    val completePersonName = Some(PersonName("Katie", None, "Test"))
    val completePersonName2 = Some(PersonName("David", None, "Test"))
    val completeRp1 = ResponsiblePerson(completePersonName)
    val completeRp2 = ResponsiblePerson(completePersonName2)
    override def view = update_services_summary(EmptyForm, AddBusinessTypeFlowModel(
      activity = Some(TrustAndCompanyServices),
      fitAndProper = Some(true),
      responsiblePeople = Some(ResponsiblePeopleFitAndProper(Set(1, 2)))
    ), Seq(), Seq((completeRp1, 1), (completeRp2, 2)))
  }

  /**
    * SimpleTCSPNoFitAndProperViewFixture.
    *
    * View to include TrustAndCompanyServices.
    * Does not includes fit and proper and has responsible person.
    */
  trait SimpleTCSPNoFitAndProperViewFixture extends ViewFixture {
    override def view = update_services_summary(EmptyForm, AddBusinessTypeFlowModel(
      activity = Some(TrustAndCompanyServices),
      fitAndProper = Some(false),
      responsiblePeople = Some(ResponsiblePeopleFitAndProper(Set(1, 2)))
    ), Seq(), Seq())
  }

  /**
    * MSBViewFixture.
    *
    * View to include MoneyServiceBusiness.
    * Has fit and proper.
    * Has responsible people.
    * Has single CurrencyExchange sub service.
    * Has new activities flagged at trading premises.
    * Has MSB services at trading premises.
    * Lists trading premises activities.
    */
  trait MSBViewFixture extends ViewFixture {
    override def view = update_services_summary(EmptyForm, AddBusinessTypeFlowModel(
      activity = Some(MoneyServiceBusiness),
      fitAndProper = Some(true),
      responsiblePeople = Some(ResponsiblePeopleFitAndProper(Set(1))),
      subSectors = Some(BusinessMatchingMsbServices(Set(CurrencyExchange))),
      areNewActivitiesAtTradingPremises = Some(true),
      tradingPremisesMsbServices = Some(BusinessMatchingMsbServices(Set(CurrencyExchange))),
      tradingPremisesActivities = Some(TradingPremisesActivities(Set(1,2)))
    ), Seq(), Seq())
  }

  /**
    * MSBViewFixture.
    *
    * View to include MoneyServiceBusiness.
    * Has fit and proper.
    * Has responsible people.
    * Has single CurrencyExchange sub service.
    * Does not have new activities flagged at trading premises.
    */
  trait MSBViewNoPremisesFixture extends ViewFixture {
    override def view = update_services_summary(EmptyForm, AddBusinessTypeFlowModel(
      activity = Some(MoneyServiceBusiness),
      fitAndProper = Some(true),
      responsiblePeople = Some(ResponsiblePeopleFitAndProper(Set(1))),
      subSectors = Some(BusinessMatchingMsbServices(Set(CurrencyExchange))),
      areNewActivitiesAtTradingPremises = Some(false)
    ), Seq(), Seq())
  }

  /**
    * MSBAllViewFixture.
    *
    * View to include MoneyServiceBusiness.
    * Has fit and proper.
    * Has responsible people.
    * Has all sub services.
    * Has new activities flagged at trading premises.
    * Has MSB services at trading premises.
    * Lists trading premises activities.
    * Has PSR No.
    */
  trait MSBAllViewFixture extends ViewFixture {
    val completePersonName = Some(PersonName("Katie", None, "Test"))
    val completeRp1 = ResponsiblePerson(completePersonName)
    val address = Address("1", "2", None, None, "AA1 1BB", None)
    override def view = update_services_summary(EmptyForm, AddBusinessTypeFlowModel(
      activity = Some(MoneyServiceBusiness),
      fitAndProper = Some(true),
      responsiblePeople = Some(ResponsiblePeopleFitAndProper(Set(1, 2))),
      subSectors = Some(BusinessMatchingMsbServices(Set(
        TransmittingMoney,
        CurrencyExchange,
        ForeignExchange,
        ChequeCashingNotScrapMetal,
        ChequeCashingScrapMetal)
      )),
      businessAppliedForPSRNumber = Some(BusinessAppliedForPSRNumberYes("111111")),
      areNewActivitiesAtTradingPremises = Some(true),
      tradingPremisesMsbServices = Some(BusinessMatchingMsbServices(Set(
        TransmittingMoney,
        CurrencyExchange,
        ForeignExchange,
        ChequeCashingNotScrapMetal,
        ChequeCashingScrapMetal))),
      tradingPremisesActivities = Some(TradingPremisesActivities(Set(1,2)))
    ), Seq((TradingPremises(None, Some(YourTradingPremises("foo", address, None, Some(new LocalDate(2010, 10, 10)), None))), 1),
           (TradingPremises(None, Some(YourTradingPremises("Bar", address, None, Some(new LocalDate(2010, 10, 10)), None))), 2)),
      Seq((completeRp1, 1)))
  }

  /**
    * MSBSingleViewFixture.
    *
    * View to include MoneyServiceBusiness.
    * Has fit and proper.
    * Has responsible people.
    * Has a single sub services.
    * Has new activities flagged at trading premises.
    * Has MSB single service at trading premises.
    * Lists trading premises activities.
    * Has PSR No.
    */
  trait MSBSingleViewFixture extends ViewFixture {
    override def view = update_services_summary(EmptyForm, AddBusinessTypeFlowModel(
      activity = Some(MoneyServiceBusiness),
      fitAndProper = Some(true),
      responsiblePeople = Some(ResponsiblePeopleFitAndProper(Set(1, 2))),
      subSectors = Some(BusinessMatchingMsbServices(Set(TransmittingMoney))),
      businessAppliedForPSRNumber = Some(BusinessAppliedForPSRNumberYes("111111")),
      areNewActivitiesAtTradingPremises = Some(true),
      tradingPremisesMsbServices = Some(BusinessMatchingMsbServices(Set(TransmittingMoney))),
      tradingPremisesActivities = Some(TradingPremisesActivities(Set(1,2)))
    ), Seq(), Seq())
  }

  /**
    * MSBNoPSRViewFixture.
    *
    * View to include MoneyServiceBusiness.
    * Has fit and proper.
    * Has responsible people.
    * Has single CurrencyExchange sub service.
    * Has new activities flagged at trading premises.
    * Has MSB services at trading premises.
    * Has no PSR No.
    */
  trait MSBNoPSRViewFixture extends ViewFixture {
    override def view = update_services_summary(EmptyForm, AddBusinessTypeFlowModel(
      activity = Some(MoneyServiceBusiness),
      fitAndProper = Some(true),
      responsiblePeople = Some(ResponsiblePeopleFitAndProper(Set(1, 2))),
      subSectors = Some(BusinessMatchingMsbServices(Set(
        TransmittingMoney)
      )),
      businessAppliedForPSRNumber = Some(BusinessAppliedForPSRNumberNo),
      areNewActivitiesAtTradingPremises = Some(true),
      tradingPremisesMsbServices = Some(BusinessMatchingMsbServices(Set(TransmittingMoney)))
    ), Seq(), Seq())
  }

  /**
    * MSBViewFixture.
    *
    * View to include MoneyServiceBusiness.
    * Has fit and proper.
    * Has responsible people.
    * Has single CurrencyExchange sub service.
    * Has new activities flagged at trading premises.
    * Has MSB services at trading premises.
    * Lists trading premises activities.
    * Has a PSR No.
    */
  trait SingleSubSectorPSRMSBViewFixture extends ViewFixture {
    override def view = update_services_summary(EmptyForm, AddBusinessTypeFlowModel(
      activity = Some(MoneyServiceBusiness),
      responsiblePeople = Some(ResponsiblePeopleFitAndProper(Set(1, 2))),
      subSectors = Some(BusinessMatchingMsbServices(Set(TransmittingMoney))),
      businessAppliedForPSRNumber = Some(BusinessAppliedForPSRNumberYes("111111")),
      areNewActivitiesAtTradingPremises = Some(true)
    ), Seq(), Seq())
  }
}
