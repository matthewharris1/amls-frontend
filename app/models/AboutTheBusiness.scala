package models

import play.api.libs.json.Json

import scala.language.implicitConversions

case class BCAddress(
                      line_1: String,
                      line_2: String,
                      line_3: Option[String] = None,
                      line_4: Option[String] = None,
                      postcode: Option[String] = None,
                      country: String) {

  def render = {
    val line3display = line_3.map(line3 => s"$line3, ").getOrElse("")
    val line4display = line_4.map(line4 => s"$line4, ").getOrElse("")
    val postcodeDisplay = postcode.map(postcode1 => s"$postcode1, ").getOrElse("")
    s"$line_1, $line_2, $line3display$line4display$postcodeDisplay$country"
  }
}

object BCAddress {
  implicit val formats = Json.format[BCAddress]
}

case class BusinessCustomerDetails(businessName: String,
                                   businessType: Option[String],
                                   businessAddress: BCAddress,
                                   sapNumber: String,
                                   safeId: String,
                                   agentReferenceNumber: Option[String],
                                   firstName: Option[String] = None,
                                   lastName: Option[String] = None)

object BusinessCustomerDetails {
  implicit val formats = Json.format[BusinessCustomerDetails]
}

case class BusinessHasWebsite(hasWebsite: Boolean, website: Option[String])

object BusinessHasWebsite {
  implicit val formats = Json.format[BusinessHasWebsite]
}

case class TelephoningBusiness(businessPhoneNumber: String,
                               mobileNumber: Option[String])

object TelephoningBusiness {
  implicit val formats = Json.format[TelephoningBusiness]
}

/**
 * This created because the address is not entered at front end so isn't in form.
 */
case class RegisteredOfficeSave4Later(registeredOfficeAddress: BCAddress,
                                      isRegisteredOffice: Boolean, isCorrespondenceAddressSame: Boolean)


object RegisteredOfficeSave4Later {
  implicit val formats = Json.format[RegisteredOfficeSave4Later]
}

case class RegisteredOffice(isRegisteredOffice: Boolean, isCorrespondenceAddressSame: Boolean)

object RegisteredOffice {

  implicit val formats = Json.format[RegisteredOffice]

  def fromRegisteredOfficeSave4Later(registeredOfficeSave4Later: RegisteredOfficeSave4Later): RegisteredOffice = {
    RegisteredOffice(registeredOfficeSave4Later.isRegisteredOffice, registeredOfficeSave4Later.isCorrespondenceAddressSame)
  }

  def fromBooleanTuple(tuple: (Boolean, Boolean)): RegisteredOffice = {
    RegisteredOffice(tuple._1, tuple._2)
  }

  def toBooleanTuple(registeredOffice: RegisteredOffice): Some[(Boolean, Boolean)] = {
    Some(Tuple2( registeredOffice.isRegisteredOffice, registeredOffice.isCorrespondenceAddressSame ))
  }

}

case class BusinessWithVAT(hasVAT: Boolean, VATNum: Option[String])

object BusinessWithVAT {
  implicit val formats = Json.format[BusinessWithVAT]
}

object BusinessHasEmail {
  implicit val formats = Json.format[BusinessHasEmail]
}

case class BusinessHasEmail(email: String)

case class RegisteredForMLR(hasDigitalMLR: Boolean, hasNonDigitalMLR: Boolean, mlrNumber: Option[String], prevMlrNumber: Option[String])

object RegisteredForMLR {
  implicit val formats = Json.format[RegisteredForMLR]

  def applyString(hasMLR: (Boolean, Boolean), mlrNumber: Option[String], prevMlrNumber: Option[String]): RegisteredForMLR = {
    RegisteredForMLR(hasMLR._1, hasMLR._2, mlrNumber, prevMlrNumber)
  }

  def unapplyString(registeredForMLR: RegisteredForMLR): (Option[((Boolean, Boolean), Option[String], Option[String])]) = {
    val tuple = (registeredForMLR.hasDigitalMLR, registeredForMLR.hasNonDigitalMLR)
    Some(Tuple3(tuple, registeredForMLR.mlrNumber, registeredForMLR.prevMlrNumber))
  }
}
