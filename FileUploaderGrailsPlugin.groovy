class FileUploaderGrailsPlugin {

    def version = "2.3"
    def grailsVersion = "2.1 > *"
    def groupId = "com.cc.plugins"
    def dependsOn = ["hibernate":"2.1 > *"]
    def pluginExcludes = [
        "grails-app/views/error.gsp",
        "src/templates"
    ]

    def loadAfter = ['hibernate']

    def author = "Federico Hofman"
    def authorEmail = "fhofman@gmail.com"
    def title = "File Uploader Grails Plugin"
    def description = '''
This plugin provides easy integration with your grails application
to handle file uploading with multiple configuration.

This is a heavily modified version with updates from visheshd, danieldbower, SAgrawal14"
'''
    def documentation = "https://github.com/causecode/grails-file-uploader"
    def organization = [ name: "CauseCode Technologies Pvt. Ltd.", url: "http://causecode.com" ]
    def scm = [ url: "https://github.com/causecode/grails-file-uploader/issues" ]

}