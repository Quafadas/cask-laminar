package example.shared

import upickle.default._

// scala 2
  object GetSuggestions {

    case class MyRequest(search: String, prefixOnly: Option[Boolean] = None)
    object MyRequest {
      implicit val codec2: ReadWriter[MyRequest] = macroRW[MyRequest]
    }

    case class MyResponse(suggestions: Seq[String])
    object MyResponse {
      implicit val codec2: ReadWriter[MyResponse] = macroRW[MyResponse]
    }
  }



// scala 3
//object Protocol {
//  object GetSuggestions {
//
//    case class MyRequest(search: String, prefixOnly: Option[Boolean] = None) derives Reader, Writer
//
//    case class MyResponse(suggestions: Seq[String]) derives Reader, Writer
//  }
//}
//