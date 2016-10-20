package models.status

import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec

class CompletionStateSpec  extends PlaySpec with MockitoSugar{

  "Completion State View Model" should {
    "return updated statuses when SubmissionDecisionApproved" in {
      val model = CompletionStateViewModel(SubmissionDecisionApproved)
      model.statuses.get(SubmissionDecisionApproved).get must be(Current)

      model.statuses.filter(entry => entry._1 != SubmissionDecisionApproved) map {
        entry => entry._2 must be(Complete)
      }
    }

    "return updated statuses when SubmissionDecisionRejected" in {
      val model = CompletionStateViewModel(SubmissionDecisionRejected)
      model.statuses.get(SubmissionDecisionRejected).get must be(Current)

      model.statuses.filter(entry => entry._1 != SubmissionDecisionRejected) map {
        entry => entry._2 must be(Complete)
      }
    }

    "return updated statuses when Not Submitted" in {
      val model = CompletionStateViewModel(NotCompleted)
      model.statuses.get(NotCompleted).get must be(Current)

      model.statuses.filter(entry => entry._1 != NotCompleted) map {
        entry => entry._2 must be(Incomplete)
      }
    }

  }

}