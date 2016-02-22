package models.tradingpremises

import models.businessmatching.BusinessActivity
import play.api.data.mapping._
import play.api.data.mapping.forms.UrlFormEncoded
import play.api.data.mapping.forms.Rules.{minLength => _, _}
import play.api.data.mapping.forms.Writes._
import utils.TraversableValidators.minLength
import utils.MappingUtils.Implicits
import play.api.libs.json.{Json, Reads}


case class WhatDoesYourBusinessDo(activities : Set[BusinessActivity])

object WhatDoesYourBusinessDo {
  implicit val formRule : Rule[UrlFormEncoded, WhatDoesYourBusinessDo] = From[UrlFormEncoded] { __ =>
    (__ \ "activities")
      .read(minLength[Set[BusinessActivity]](1))
      .fmap(WhatDoesYourBusinessDo.apply _)
  }

  implicit val formWrite = Write[WhatDoesYourBusinessDo, UrlFormEncoded] { data =>
    Map("activities" -> data.activities.toSeq.map(BusinessActivity.activityFormWrite.writes))
  }

  implicit val format = Json.format[WhatDoesYourBusinessDo]
}