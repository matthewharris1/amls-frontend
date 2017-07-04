/*
 * Copyright 2017 HM Revenue & Customs
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

package models.responsiblepeople

import org.joda.time.LocalDate
import play.Logger
import play.api.libs.json.Reads
import typeclasses.MongoKey
import models.registrationprogress.{Completed, NotStarted, Section, Started}
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.StatusConstants

import scala.collection.Seq

case class ResponsiblePeople(personName: Option[PersonName] = None,
                             personResidenceType: Option[PersonResidenceType] = None,
                             ukPassport: Option[UKPassport] = None,
                             nonUKPassport: Option[NonUKPassport] = None,
                             dateOfBirth: Option[DateOfBirth] = None,
                             contactDetails: Option[ContactDetails] = None,
                             addressHistory: Option[ResponsiblePersonAddressHistory] = None,
                             positions: Option[Positions] = None,
                             saRegistered: Option[SaRegistered] = None,
                             vatRegistered: Option[VATRegistered] = None,
                             experienceTraining: Option[ExperienceTraining] = None,
                             training: Option[Training] = None,
                             hasAlreadyPassedFitAndProper: Option[Boolean] = None,
                             hasChanged: Boolean = false,
                             lineId: Option[Int] = None,
                             status: Option[String] = None,
                             endDate:Option[ResponsiblePersonEndDate] = None,
                             soleProprietorOfAnotherBusiness: Option[SoleProprietorOfAnotherBusiness] = None
                            ) {

  def personName(p: PersonName): ResponsiblePeople =
    this.copy(personName = Some(p), hasChanged = hasChanged || !this.personName.contains(p))

  def personResidenceType(p: PersonResidenceType): ResponsiblePeople =
    this.copy(personResidenceType = Some(p), hasChanged = hasChanged || !this.personResidenceType.contains(p))

  def personResidenceType(p: Option[PersonResidenceType]): ResponsiblePeople =
    this.copy(personResidenceType = p, hasChanged = hasChanged || this.personResidenceType != p)

  def contactDetails(p: ContactDetails): ResponsiblePeople =
    this.copy(contactDetails = Some(p), hasChanged = hasChanged || !this.contactDetails.contains(p))

  def saRegistered(p: SaRegistered): ResponsiblePeople =
    this.copy(saRegistered = Some(p), hasChanged = hasChanged || !this.saRegistered.contains(p))

  def addressHistory(p: ResponsiblePersonAddressHistory): ResponsiblePeople =
    this.copy(addressHistory = Some(p), hasChanged = hasChanged || !this.addressHistory.contains(p))

  def positions(p: Positions): ResponsiblePeople =
    this.copy(positions = Some(p), hasChanged = hasChanged || !this.positions.contains(p))

  def soleProprietorOfAnotherBusiness(p: SoleProprietorOfAnotherBusiness): ResponsiblePeople =
    this.copy(soleProprietorOfAnotherBusiness = Some(p), hasChanged = hasChanged || !this.soleProprietorOfAnotherBusiness.contains(p))

  def vatRegistered(p: VATRegistered): ResponsiblePeople =
    this.copy(vatRegistered = Some(p), hasChanged = hasChanged || !this.vatRegistered.contains(p))

  def experienceTraining(p: ExperienceTraining): ResponsiblePeople =
    this.copy(experienceTraining = Some(p), hasChanged = hasChanged || !this.experienceTraining.contains(p))

  def training(p: Training): ResponsiblePeople =
    this.copy(training = Some(p), hasChanged = hasChanged || !this.training.contains(p))

  def hasAlreadyPassedFitAndProper(p: Boolean): ResponsiblePeople =
    this.copy(hasAlreadyPassedFitAndProper = Some(p), hasChanged = hasChanged || !this.hasAlreadyPassedFitAndProper.contains(p))

  def ukPassport(p: UKPassport): ResponsiblePeople =
    this.copy(ukPassport = Some(p), hasChanged = hasChanged || !this.ukPassport.contains(p))

  def nonUKPassport(p: NonUKPassport): ResponsiblePeople =
    this.copy(nonUKPassport = Some(p), hasChanged = hasChanged || !this.nonUKPassport.contains(p))

  def dateOfBirth(p: DateOfBirth): ResponsiblePeople =
    this.copy(dateOfBirth = Some(p), hasChanged = hasChanged || !this.dateOfBirth.contains(p))

  def status(p: String): ResponsiblePeople =
    this.copy(status = Some(p), hasChanged = hasChanged || !this.status.contains(p))

  def checkVatField(otherBusinessSP: Option[SoleProprietorOfAnotherBusiness]): Boolean = {
    otherBusinessSP.fold(true) { x =>
      x.soleProprietorOfAnotherBusiness match {
        case true => this.vatRegistered.isDefined
        case false => this.vatRegistered.isEmpty
      }
    }
  }

  def isComplete: Boolean = {
    Logger.debug(s"[ResponsiblePeople][isComplete] $this")
    this match {
      case ResponsiblePeople(Some(_), Some(_), _, _, _, Some(_), Some(_), Some(pos),
      Some(_), _, Some(_), Some(_), _, _, _, _, _, otherBusinessSP) if pos.startDate.isDefined && checkVatField(otherBusinessSP)=> true
      case ResponsiblePeople(None, None, None, None, None, None, None, None, None, None, None, None, None, _, _, _, _, None) => true
      case _ => false
    }
  }

  def isNominatedOfficer: Boolean = positions match {
    case Some(pos) => pos.isNominatedOfficer
    case None => false
  }
}

object ResponsiblePeople {

  def anyChanged(newModel: Seq[ResponsiblePeople]): Boolean = {
    newModel exists {
      _.hasChanged
    }
  }

  implicit val formatOption = Reads.optionWithNull[Seq[ResponsiblePeople]]

  def section(implicit cache: CacheMap): Section = {

    val messageKey = "responsiblepeople"
    val notStarted = Section(messageKey, NotStarted, false, controllers.responsiblepeople.routes.ResponsiblePeopleAddController.get())

    def filter(rp: Seq[ResponsiblePeople]) = rp.filterNot(_.status.contains(StatusConstants.Deleted)).filterNot(_ == ResponsiblePeople())

    cache.getEntry[Seq[ResponsiblePeople]](key).fold(notStarted) { rp =>

      if(filter(rp).equals(Nil)) {
        Section(messageKey, NotStarted, anyChanged(rp), controllers.responsiblepeople.routes.ResponsiblePeopleAddController.get())
      } else {
        rp match {
          case responsiblePeople if responsiblePeople.nonEmpty && responsiblePeople.forall {
            _.isComplete
          } => Section(messageKey, Completed, anyChanged(rp), controllers.responsiblepeople.routes.YourAnswersController.get())
          case responsiblePeople => {
            val index = responsiblePeople.indexWhere {
              case model if !model.isComplete => true
              case _ => false
            }
            Section(messageKey, Started, anyChanged(rp), controllers.responsiblepeople.routes.WhoMustRegisterController.get(index + 1))
          }
        }
      }
    }

  }

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  val flowChangeOfficer = "changeofficer"
  val flowFromDeclaration = "fromDeclaration"

  val key = "responsible-people"

  implicit val mongoKey = new MongoKey[ResponsiblePeople] {
    override def apply(): String = key
  }

  def constant[A](x: A): Reads[A] = new Reads[A] {
    override def reads(json: JsValue): JsResult[A] = JsSuccess(x)
  }

  def ifPersonResidenceType[A](oldreader: Reads[Option[A]]): Reads[Option[A]] = {
    (__ \ "personResidenceType").readNullable[JsObject] flatMap {
      case Some(_) => oldreader
      case _ => constant[Option[A]](None)
    }
  }

  implicit val writes: Writes[ResponsiblePeople] = Json.writes[ResponsiblePeople]

  def oldNonUkPassportReader: Reads[Option[NonUKPassport]] = {
    val ppReader: Reads[Option[NonUKPassport]] = (__ \ "personResidenceType" \ "nonUKPassportNumber").readNullable[String] map {
      case Some(p) => Some(NonUKPassportYes(p))
      case _ => Some(NoPassport)
    }

    ifPersonResidenceType(ppReader)
  }

  val nonUkPassportReader: Reads[Option[NonUKPassport]] = {
    (__ \ "nonUKPassport").readNullable[NonUKPassport] flatMap {
      case None => oldNonUkPassportReader
      case p => constant(p)
    }
  }

  def oldUkPassportReader: Reads[Option[UKPassport]] = {
    val oppReader: Reads[Option[UKPassport]] = (__ \ "personResidenceType" \ "ukPassportNumber").readNullable[String] map {
      case Some(p) => Some(UKPassportYes(p))
      case _ => Some(UKPassportNo)
    }

    ifPersonResidenceType(oppReader)
  }

  val UkPassportReader: Reads[Option[UKPassport]] = {
    (__ \ "ukPassport").readNullable[UKPassport] flatMap {
      case None => oldUkPassportReader
      case p => constant(p)
    }
  }

  def oldDateOfBirthReader: Reads[Option[DateOfBirth]] = {
    val dobReader = (__ \ "personResidenceType" \ "dateOfBirth").readNullable[LocalDate] map {
      case Some(p) => Some(DateOfBirth(p))
      case _ => None
    }

    ifPersonResidenceType(dobReader)
  }

  val UkDateOfBirthReader: Reads[Option[DateOfBirth]] = {
    (__ \ "dateOfBirth").readNullable[DateOfBirth] flatMap {
      case None => oldDateOfBirthReader
      case p => constant(p)
    }
  }

  implicit val reads: Reads[ResponsiblePeople] = {
    (
      (__ \ "personName").readNullable[PersonName] and
        (__ \ "personResidenceType").readNullable[PersonResidenceType] and
        __.read(UkPassportReader) and
        __.read(nonUkPassportReader) and
        __.read(UkDateOfBirthReader) and
        (__ \ "contactDetails").readNullable[ContactDetails] and
        (__ \ "addressHistory").readNullable[ResponsiblePersonAddressHistory] and
        (__ \ "positions").readNullable[Positions] and
        (__ \ "saRegistered").readNullable[SaRegistered] and
        (__ \ "vatRegistered").readNullable[VATRegistered] and
        (__ \ "experienceTraining").readNullable[ExperienceTraining] and
        (__ \ "training").readNullable[Training] and
        (__ \ "hasAlreadyPassedFitAndProper").readNullable[Boolean] and
        (__ \ "hasChanged").readNullable[Boolean].map {
          _.getOrElse(false)
        } and
        (__ \ "lineId").readNullable[Int] and
        (__ \ "status").readNullable[String] and
        (__ \ "endDate").readNullable[ResponsiblePersonEndDate] and
        (__ \ "soleProprietorOfAnotherBusiness").readNullable[SoleProprietorOfAnotherBusiness]
      ).tupled.map { t =>
        val r = (ResponsiblePeople.apply _).tupled(t)

        if (hasUkPassportNumber(r)) {
          r.copy(nonUKPassport = None)
        } else if (!hasUkPassportNumber(r) && !hasNonUkPassportNumber(r) && !hasDateOfBirth(r)) {
          r.copy(ukPassport = None, nonUKPassport = None)
        } else r
      }
  }

  private def hasUkPassportNumber(rp: ResponsiblePeople): Boolean = rp.ukPassport match {
    case Some(UKPassportYes(_)) => true
    case _ => false
  }
  private def hasNonUkPassportNumber(rp: ResponsiblePeople): Boolean = rp.nonUKPassport match {
    case Some(NonUKPassportYes(_)) => true
    case _ => false
  }
  private def hasDateOfBirth(rp: ResponsiblePeople): Boolean = rp.dateOfBirth.isDefined

  def default(responsiblePeople: Option[ResponsiblePeople]): ResponsiblePeople =
    responsiblePeople.getOrElse(ResponsiblePeople())

}
