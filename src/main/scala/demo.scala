// import scala.concurrent.Future


// class MyController extends Controller {

//   def myAction(param: String): EssentialAction = Action { implicit request =>
//     Ok(s"Received $request with param=$param")
//   }

//   def myJsonAction: Action[JsValue] = Action.async(parse.json) { implicit request =>
//     val bodyAsString = Json.stringify(request.body)
//     Future.successful(Ok("Received $request with body: $bodyAsString"))
//   }
// }

// trait RequestHeader
// case class Request[B](rh: RequestHeader, body: B) extends RequestHeader
// case class Result(body: String)

// object Ok {
//   def apply(body: String): Result = Result(body)
// }

// trait EssentialAction extends (RequestHeader => Future[Result])

// trait BodyParser[B] {
//   def parse(rh: RequestHeader): B
// }

// trait JsValue

// object BodyParsers extends BodyParsers
// trait BodyParsers {
//   val anyContent: BodyParser[AnyContent] = _ => new AnyContent {}
//   val json: BodyParser[JsValue] = _ => new JsValue {}
// }

// class ActionBuilder[B](parser: BodyParser[B]) {

//   def apply(block: RequestHeader => Result): EssentialAction = {
//     rh => Future.successful(block(rh))
//   }

//   def apply(block: => Result): EssentialAction = {
//     rh => Future.successful(block)
//   }

//   def async(block: => Future[Result]): EssentialAction = {
//     rh => block
//   }

//   def async(block: RequestHeader => Future[Result]): EssentialAction = {
//     rh => block(rh)
//   }

//   def async[B](parser: BodyParser[B])(block: Request[B] => Future[Result]): Action[B] = {
//     rh => block(Request(rh, parser.parse(rh)))
//   }
// }

// trait AnyContent

// object Action extends ActionBuilder[AnyContent](BodyParsers.anyContent)
// trait Action[B] extends EssentialAction

// trait Controller {
//   object parse extends BodyParsers
// }

// object Json {
//   def stringify(json: JsValue): String = "{}"
// }
