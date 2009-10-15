class PdfController {

  PdfService pdfService

  def index = { redirect(action: demo) }

  def pdfLink = {
    try{
      byte[] b
      def baseUri = request.scheme + "://" + request.serverName + ":" + request.serverPort + grailsAttributes.getApplicationUri(request)
      if(params.pdfController || params.template) {
        def content
        if(params.template){
          println "Template: $params.template"
          content = g.render(template:params.template, model:[pdf:params])
        }
        else{
          println "GSP - Controller: $params.pdfController , Action: $params.pdfAction"
          def controllerName = params.pdfController.substring(0,1).toUpperCase() + params.pdfController.substring(1) + "Controller"
          def controller = grailsApplication.mainContext.getBean(controllerName)
          def model = controller[params.pdfAction].call()
          def template = "/${params.pdfController}/${params.pdfAction}"
          content = g.render(template:template, model:model)
        }
        b = pdfService.buildPdfFromString(content)
      }
      else{
        def url = baseUri + params.url
        b = pdfService.buildPdf(url)
      }
      response.setContentType("application/pdf")
      response.setHeader("Content-disposition", "attachment; filename=" + (params.filename ?: "document.pdf"))
      response.setContentLength(b.length)
      response.getOutputStream().write(b)
    }
    catch (Throwable e) {
      println "there was a problem with PDF generation ${e}"
      redirect(uri:params.url + '?' + request.getQueryString())
    }
  }

  def pdfForm = {
    try{
      byte[] b
      def baseUri = request.scheme + "://" + request.serverName + ":" + request.serverPort + grailsAttributes.getApplicationUri(request)
      if(request.method == "GET") {
        def url = baseUri + params.url + '?' + request.getQueryString()
        //println "BaseUri is $baseUri"
        //println "Fetching url $url"
        b = pdfService.buildPdf(url)
      }
      if(request.method == "POST"){
        def content
        if(params.template){
          //println "Template: $params.template"
          content = g.render(template:params.template, model:[pdf:params])
        }
        else{
          //println "GSP - Controller: $params.pdfController , Action: $params.pdfAction"
          // content = g.include(controller:params.pdfController, action:params.pdfAction, id:params.id, params:params)
        }
        b = pdfService.buildPdfFromString(content, baseUri)
      }
      response.setContentType("application/pdf")
      response.setHeader("Content-disposition", "attachment; filename=" + (params.filename ?: "document.pdf"))
      response.setContentLength(b.length)
      response.getOutputStream().write(b)
    }
    catch (Throwable e) {
      println "there was a problem with PDF generation ${e}"
      redirect(uri:params.url + '?' + request.getQueryString())
    }
  }

  def demo = {
    def firstName = params.first ?: "Eric"
    def lastName = params.last ?: "Cartman"
    def age = params.age
    return [firstName:firstName, lastName:lastName, age:age]
  }

  def demo2 = {
    def id = params.id
    def name = params.name
    def age = params.age
    def randomString = params.randomString ?: "PDF creation is a blast!!!"
    def food = params.food
    def hometown = params.hometown
    return [id:id, name:name, age:age, randomString:randomString, food:food, hometown:hometown]
  }

  def demo3 = {
    def today = new Date()
    def tomorrow = today +1
    def content = g.include(controller:"pdf", action:"sampleInclude", params:['today':today, 'tomorrow':tomorrow])
    return ['content':content, 'pdf':params, 'id':params.id]
  }

  def sampleInclude = {
    def bar = 'foo'
    def today = params?.today
    def tomorrow = params?.tomorrow
    return ['bar':bar, 'today':today, 'tomorrow':tomorrow]
    //[today:today, tomorrow:tomorrow]
  }

}

