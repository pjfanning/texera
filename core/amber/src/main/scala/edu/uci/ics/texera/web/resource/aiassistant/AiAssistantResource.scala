package edu.uci.ics.texera.web.resource
import edu.uci.ics.texera.web.resource.aiassistant.AiAssistantManager
import javax.annotation.security.RolesAllowed
import javax.ws.rs._
import javax.ws.rs.core.{MediaType, Response}
import io.dropwizard.auth.Auth
import edu.uci.ics.texera.web.auth.SessionUser

@Path("/aiassistant")
class AiAssistantResource {
  final private lazy val isEnabled = AiAssistantManager.validAIAssistant
  @GET
  @RolesAllowed(Array("REGULAR", "ADMIN"))
  @Path("/isenabled")
  def isAiAssistantEnable: String = isEnabled

  /**
    * Endpoint to get the operator comment from OpenAI.
    * @param prompt The input prompt for the OpenAI model.
    * @param user The authenticated session user.
    * @return A response containing the generated comment from OpenAI or an error message.
    */
  @POST
  @Path("/generateComment")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def getAiComment(prompt: String, @Auth user: SessionUser): Response = {
    // Prepare the final prompt by escaping necessary characters
    val finalPrompt = prompt.replace("\\", "\\\\").replace("\"", "\\\"")

    // Create the JSON request body
    val requestBody =
      s"""
         |{
         |  "model": "gpt-4o",
         |  "messages": [{"role": "user", "content": "$finalPrompt"}],
         |  "max_tokens": 10
         |}
       """.stripMargin

    try {
      // Set up the connection to the OpenAI API
      val url = new java.net.URL("https://api.openai.com/v1/chat/completions")
      val connection = url.openConnection().asInstanceOf[java.net.HttpURLConnection]
      connection.setRequestMethod("POST")
      connection.setRequestProperty("Authorization", s"Bearer ${AiAssistantManager.accountKey}")
      connection.setRequestProperty("Content-Type", "application/json")
      connection.setDoOutput(true)

      // Send the request to OpenAI API
      connection.getOutputStream.write(requestBody.getBytes("UTF-8"))

      // Get the response code and content from the API
      val responseCode = connection.getResponseCode
      val responseStream = connection.getInputStream
      val responseString = scala.io.Source.fromInputStream(responseStream).mkString

      // Close the stream and disconnect
      responseStream.close()
      connection.disconnect()

      // Return the response from the API
      Response.status(responseCode).entity(responseString).build()
    } catch {
      // Handle exceptions and return an error response
      case e: Exception =>
        e.printStackTrace()
        Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error occurred").build()
    }
  }

  /**
   * Endpoint to get the summary comment from OpenAI.
   * @param request The request body containing model, max_tokens, and prompt.
   * @param user The authenticated session user.
   * @return A response containing the generated summary comment from OpenAI or an error message.
   */
  @POST
  @Path("/openai")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getAiSummaryComment(request: AiSummaryRequest, @Auth user: SessionUser): Response = {

    println(s"Received request: $request")
    // Prepare the final prompt by escaping necessary characters
    val finalPrompt = request.prompt.replace("\\", "\\\\").replace("\"", "\\\"")

    // Create the JSON request body
    val requestBody =
      s"""
         |{
         |  "model": "${request.model}",
         |  "messages": [{"role": "user", "content": "$finalPrompt"}],
         |  "max_tokens": ${request.maxTokens}
         |}
     """.stripMargin

    println(s"requestBody $requestBody")

    try {
      // Set up the connection to the OpenAI API
      val url = new java.net.URL("https://api.openai.com/v1/chat/completions")
      val connection = url.openConnection().asInstanceOf[java.net.HttpURLConnection]
      connection.setRequestMethod("POST")
      connection.setRequestProperty("Authorization", s"Bearer ${AiAssistantManager.accountKey}")
      connection.setRequestProperty("Content-Type", "application/json")
      connection.setDoOutput(true)

      // Send the request to OpenAI API
      connection.getOutputStream.write(requestBody.getBytes("UTF-8"))

      // Get the response code and content from the API
      val responseCode = connection.getResponseCode
      val responseStream = if (responseCode >= 200 && responseCode < 300) {
        connection.getInputStream
      } else {
        connection.getErrorStream
      }
      val responseString = scala.io.Source.fromInputStream(responseStream).mkString

      // Close the stream and disconnect
      responseStream.close()
      connection.disconnect()

      // Return the response from the API
      Response.status(responseCode).entity(responseString).build()
    } catch {
      // Handle exceptions and return an error response
      case e: Exception =>
        e.printStackTrace()
        Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(s"Error occurred: ${e.getMessage}").build()
    }
  }
}
// Move AiSummaryRequest to an external case class
case class AiSummaryRequest(model: String, maxTokens: Int, prompt: String)
