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

import forms.InvalidForm
import models.businessactivities._
import models.eab.Eab
import models.responsiblepeople._
import models.supervision._
import org.joda.time.LocalDate
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import scala.collection.immutable.ListMap

class ControllerHelperSpec extends AmlsSpec with ResponsiblePeopleValues with DependencyMocks {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .build()

  def createCompleteResponsiblePersonSeq: Option[Seq[ResponsiblePerson]] = Some(Seq(completeResponsiblePerson))

  def createRPsWithMissingDoB: Option[Seq[ResponsiblePerson]] = {
    val inCompleteResponsiblePerson: ResponsiblePerson = completeResponsiblePerson.copy(dateOfBirth = None)

    Some(
      Seq(
        completeResponsiblePerson,
        inCompleteResponsiblePerson,
        completeResponsiblePerson
      )
    )
  }

  val completeRedressScheme = Json.obj(
    "redressScheme" -> "propertyOmbudsman"
  )
  val eabPropertyOmbudsman = Eab(completeRedressScheme,  hasAccepted = true)

  val completeRedressSchemePropertyRedress = Json.obj(
    "redressScheme" -> "propertyRedressScheme"
  )
  val eabPropertyRedress = Eab(completeRedressSchemePropertyRedress,  hasAccepted = true)

  val completeRedressSchemeOmbudsmanServices = Json.obj(
    "redressScheme" -> "ombudsmanServices"
  )
  val eabOmbudsmanServices = Eab(completeRedressSchemeOmbudsmanServices,  hasAccepted = true)

  val completeRedressSchemeOther = Json.obj(
    "redressScheme" -> "other"
  )
  val eabOther = Eab(completeRedressSchemeOther,  hasAccepted = true)

  val eabNoRedress = Eab(Json.obj(),  hasAccepted = true)

  val accountantNameCompleteModel = Some(BusinessActivities(
    whoIsYourAccountant = Some(WhoIsYourAccountant(
      Some(WhoIsYourAccountantName("Accountant name", None)),
      Some(WhoIsYourAccountantIsUk(true)),
      Some(UkAccountantsAddress("", "", None, None, ""))))))

  val accountantNameInCompleteModel = Some(BusinessActivities(
    whoIsYourAccountant = None))

  val completeRpAndNotNominated = completeResponsiblePerson.copy(positions = Some(Positions(Set(BeneficialOwner), Some(PositionStartDate(LocalDate.now())))))
  val inCompleteRpAndNominated = completeResponsiblePerson.copy(approvalFlags = ApprovalFlags(None, None))
  val inCompleteRpAndNotNominated = completeResponsiblePerson.copy(personName = None, positions = Some(Positions(Set(BeneficialOwner), Some(PositionStartDate(LocalDate.now())))))

  val oneCompleteNominatedOff: Option[Seq[ResponsiblePerson]] = {
    Some(Seq(completeResponsiblePerson,
      completeRpAndNotNominated,
      incompleteResponsiblePeople,
      inCompleteRpAndNotNominated
    ))
  }

  val oneInCompleteNominatedOff: Option[Seq[ResponsiblePerson]] = {
    Some(Seq(inCompleteRpAndNominated,
      completeRpAndNotNominated,
      incompleteResponsiblePeople,
      inCompleteRpAndNotNominated
    ))
  }

  val inCompleteAndNoNominatedOff: Option[Seq[ResponsiblePerson]] = {
    Some(Seq(inCompleteRpAndNotNominated,
      incompleteResponsiblePeople
    ))
  }

  "ControllerHelper" must {

    "hasInvalidRedressScheme" must {

      "return false" when {

        "eab is None" in {
          ControllerHelper.hasInvalidRedressScheme(None) mustEqual false
        }

        "when no redress scheme" in {
          ControllerHelper.hasInvalidRedressScheme(Some(eabNoRedress)) mustEqual false
        }

        "when 'PropertyOmbudsman' is selected" in {
          ControllerHelper.hasInvalidRedressScheme(Some(eabPropertyOmbudsman)) mustEqual false
        }

        "when 'PropertyRedressScheme' is selected" in {
          ControllerHelper.hasInvalidRedressScheme(Some(eabPropertyRedress)) mustEqual false
        }
      }

      "return true" when {

        "when 'OmbudsmanServices' is selected" in {
          ControllerHelper.hasInvalidRedressScheme(Some(eabOmbudsmanServices)) mustEqual true
        }

        "when 'Other' is selected" in {
          ControllerHelper.hasInvalidRedressScheme(Some(eabOther)) mustEqual true
        }
      }
    }

    "hasIncompleteResponsiblePerson" must {

      "return false" when {
        "responsiblePerson seq is None" in {
          ControllerHelper.hasIncompleteResponsiblePerson(None) mustEqual false
        }

        "all responsiblePerson are complete" in {
          ControllerHelper.hasIncompleteResponsiblePerson(createCompleteResponsiblePersonSeq) mustEqual false
        }
      }

      "return true" when {
        "any responsiblePerson is not complete" in {
          ControllerHelper.hasIncompleteResponsiblePerson(createRPsWithMissingDoB) mustEqual true
        }
      }
    }

    "anotherBodyComplete" must {
      "return tuple of (is anotherBodyComplete, is anotherBodyYes) for AnotherBodyNo " in {
        val supervision = Supervision(Some(AnotherBodyNo))

        ControllerHelper.anotherBodyComplete(supervision) mustBe Some(true, false)
      }

      "return tuple of (is anotherBodyComplete, is anotherBodyYes) for AnotherBodyYes " in {
        val supervision = Supervision(Some(AnotherBodyYes(supervisorName = "Name")))

        ControllerHelper.anotherBodyComplete(supervision) mustBe Some(false, true)
      }

      "return tuple of (is anotherBodyComplete, is anotherBodyYes) for complete AnotherBodyYes " in {
        val supervision = Supervision(Some(AnotherBodyYes("Name",
          Some(SupervisionStart(new LocalDate(1990, 2, 24))),
          Some(SupervisionEnd(new LocalDate(1998, 2, 24))),
          Some(SupervisionEndReasons("Reason")))))

        ControllerHelper.anotherBodyComplete(supervision) mustBe Some(true, true)
      }
    }

    "isAnotherBodyYes" must {
      "return true if AnotherBody is instance of AnotherBodyYes" in {
        val supervision = Supervision(Some(AnotherBodyYes(supervisorName = "Name")))

        ControllerHelper.isAnotherBodyYes(ControllerHelper.anotherBodyComplete(supervision)) mustBe true
      }

      "return false if AnotherBody is AnotherBodyNo" in {
        val supervision = Supervision(Some(AnotherBodyNo))

        ControllerHelper.isAnotherBodyYes(ControllerHelper.anotherBodyComplete(supervision)) mustBe false
      }
    }

    "isAnotherBodyComplete" must {
      "return true if AnotherBodyYes is complete" in {
        val supervision = Supervision(Some(AnotherBodyYes("Name",
          Some(SupervisionStart(new LocalDate(1990, 2, 24))),
          Some(SupervisionEnd(new LocalDate(1998, 2, 24))),
          Some(SupervisionEndReasons("Reason")))))

        ControllerHelper.isAnotherBodyComplete(ControllerHelper.anotherBodyComplete(supervision)) mustBe true
      }

      "return false if AnotherBodyYes is incomplete" in {
        val supervision = Supervision(Some(AnotherBodyYes("Name",
          Some(SupervisionStart(new LocalDate(1990, 2, 24))),
          Some(SupervisionEnd(new LocalDate(1998, 2, 24))))))

        ControllerHelper.isAnotherBodyComplete(ControllerHelper.anotherBodyComplete(supervision)) mustBe false
      }

      "return true if AnotherBody is AnotherBodyNo" in {
        val supervision = Supervision(Some(AnotherBodyNo))

        ControllerHelper.isAnotherBodyComplete(ControllerHelper.anotherBodyComplete(supervision)) mustBe true
      }
    }

    "return accountant name" when {
      "accountant name is called with complete model" in {
        ControllerHelper.accountantName(accountantNameCompleteModel) mustEqual "Accountant name"
      }
    }

    "return empty string" when {
      "accountant name is called with incomplete model" in {
        ControllerHelper.accountantName(accountantNameInCompleteModel) mustEqual ""
      }
    }

    "supervisionComplete" must {
      "return false if supervision section is incomplete" in {
        ControllerHelper.supervisionComplete(mockCacheMap) mustBe false
      }

      "return true if supervision section is complete" in {
        val complete = mock[Supervision]

        when(complete.isComplete) thenReturn true
        when(mockCacheMap.getEntry[Supervision]("supervision")) thenReturn Some(complete)

        ControllerHelper.supervisionComplete(mockCacheMap) mustBe true
      }

      "return false if supervision section is not available" in {
        val complete = mock[Supervision]

        when(complete.isComplete) thenReturn false
        when(mockCacheMap.getEntry[Supervision]("supervision")) thenReturn None

        ControllerHelper.supervisionComplete(mockCacheMap) mustBe false
      }
    }

    "isAbComplete" must {

      val start = Some(SupervisionStart(new LocalDate(1990, 2, 24)))  //scalastyle:off magic.number
      val end = Some(SupervisionEnd(new LocalDate(1998, 2, 24)))//scalastyle:off magic.number
      val reason = Some(SupervisionEndReasons("Reason"))

      "return true if another body is AnotherBodyNo" in {
        ControllerHelper.isAbComplete(AnotherBodyNo) mustBe true
      }

      "return true if another body is complete AnotherBodyYes" in {
        ControllerHelper.isAbComplete(AnotherBodyYes("Supervisor", start, end, reason)) mustBe true
      }

      "return false if another body is incomplete AnotherBodyYes" in {
        ControllerHelper.isAbComplete(AnotherBodyYes("Supervisor", None, end, reason)) mustBe false
      }
    }

    "stripEmptyValuesFromFormWithArray" must {

      val csrfToken:Seq[String] = Seq("123456789")

      "return only nonEmpty fields where form contains data from one array" in {


        val formData: ListMap[String, Seq[String]] =
          ListMap(("csrfToken", csrfToken),
            ("currencies[0]", Seq("")),
            ("currencies[1]", Seq("value1")),
            ("currencies[2]", Seq("")),
            ("currencies[3]", Seq("value2")),
            ("currencies[4]", Seq("value3")))

        val form = InvalidForm(data = formData, Seq())
        val updatedForm: InvalidForm = ControllerHelper.stripEmptyValuesFromFormWithArray(form, "currencies")

        updatedForm.data.size mustBe 4

        updatedForm.data.get("csrfToken") mustBe Some(csrfToken)
        updatedForm.data.get("currencies[0]") mustBe Some(Seq("value1"))
        updatedForm.data.get("currencies[1]") mustBe Some(Seq("value2"))
        updatedForm.data.get("currencies[2]") mustBe Some(Seq("value3"))
      }

      "return only nonEmpty fields where form contains data from two interleaved arrays" in {
        val formData: ListMap[String, Seq[String]] =
          ListMap(("csrfToken", csrfToken),
            ("countries[0]", Seq("value1a")),
            ("""countries\[0\]-autocomp""", Seq("value1b")),
            ("countries[1]", Seq("")),
            ("""countries\[1\]-autocomp""", Seq("")),
            ("countries[2]", Seq("value2a")),
            ("""countries\[2\]-autocomp""", Seq("value2b")),
            ("countries[3]", Seq("")),
            ("""countries\[3\]-autocomp""", Seq("")),
            ("countries[4]", Seq("value3a")),
            ("""countries\[4\]-autocomp""", Seq("value3b")))

        val form = InvalidForm(data = formData, Seq())
        val updatedForm: InvalidForm = ControllerHelper.stripEmptyValuesFromFormWithArray(form, "countries", index => index / 2)

        updatedForm.data.size mustBe 7

        updatedForm.data.get("csrfToken") mustBe Some(csrfToken)
        updatedForm.data.get("countries[0]") mustBe Some(Seq("value1a"))
        updatedForm.data.get("""countries\[0\]-autocomp""") mustBe Some(Seq("value1b"))
        updatedForm.data.get("countries[1]") mustBe Some(Seq("value2a"))
        updatedForm.data.get("""countries\[1\]-autocomp""") mustBe Some(Seq("value2b"))
        updatedForm.data.get("countries[2]") mustBe Some(Seq("value3a"))
        updatedForm.data.get("""countries\[2\]-autocomp""") mustBe Some(Seq("value3b"))
      }
    }

    "hasCompleteNominatedOfficer" must {
      "return true if there is complete nominated officer in the cache" in {
        ControllerHelper.hasCompleteNominatedOfficer(oneCompleteNominatedOff) mustBe true
      }

      "return false if there is incomplete nominated officer in the cache" in {
        ControllerHelper.hasCompleteNominatedOfficer(oneInCompleteNominatedOff) mustBe false
      }

      "return false if there are incomplete rps in the cache" in {
        ControllerHelper.hasCompleteNominatedOfficer(inCompleteAndNoNominatedOff) mustBe false
      }
    }

    "completeNominatedOfficerTitleName" must {
      "return officer name if there is complete nominated officer in the cache" in {
        ControllerHelper.completeNominatedOfficerTitleName(oneCompleteNominatedOff) mustBe Some("ANSTY DAVID")
      }

      "return empty string if there is incomplete nominated officer in the cache" in {
        ControllerHelper.completeNominatedOfficerTitleName(oneInCompleteNominatedOff) mustBe Some("")
      }

      "return empty string if there are incomplete rps in the cache" in {
        ControllerHelper.completeNominatedOfficerTitleName(inCompleteAndNoNominatedOff) mustBe Some("")
      }
    }
  }
}
