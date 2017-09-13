@groovy.transform.TupleConstructor
class Runner implements Serializable{
	//Testsuit Name
	boolean Login = false
	
	//Func
    boolean AllReport = true
    boolean ExportToReportExcel = true
    boolean All = false
    boolean IsSmoke = false
    boolean IsSanity = false
    boolean activeFalse = false
    
    def get_tag() {                                 
        if(IsSmoke){
            return 'smoke'
        }
        else if(IsSanity)
        {
            return 'sanity'
        }
        else{
            return 'activeANDregression'
        }
    }
}

Runner runner = new Runner()
parallel firstBranch: {
	node ('atlas_WinOS_New') {
		env.WORKSPACE='D:\\AtlasBuffet'
		env.outputPath='D:\\Output\\Prod'
		
		dir (env.WORKSPACE){
			stage ('SCGP_QATraceBack_Login_Production')
			{
				run_SCGP_QATraceBack_Login_Production(runner)
			}
			stage ('MyAIS_AllReport'){
				run_all_report(runner)
			}
			stage ('MyAIS_ExportToReportExcel'){
				run_export_to_report_excel(runner)
			}
		}
	}
}, failFast: false

def getTime(){
	time = new Date().format("yyyy-MM-dd HH:mm:ss",TimeZone.getTimeZone("ICT"))
	return time
}

def run_SCGP_QATraceBack_Login_Production(runner){
	if(runner.Login || runner.All){
		dir(env.WORKSPACE) {
			//Check out
			checkout([$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: false, ignoreDirPropChanges: false, includedRegions: '', locations: [[credentialsId: 'Tinpan', depthOption: 'infinity', ignoreExternalsOption: true, local: 'AisRobotBuffet', remote: 'https://matador.toro.ais/svn/ProjectDocument/OSD/E2E/Automate/RobotFramework/AisRobotBuffet'], [credentialsId: 'Tinpan', depthOption: 'infinity', ignoreExternalsOption: true, local: 'MyAIS', remote: 'https://matador.toro.ais/svn/ProjectDocument/OSD/E2E/Automate/RobotFramework/MyAIS']], workspaceUpdater: [$class: 'UpdateUpdater']])
			//RUN
			bat "if exist " + env.outputPath + "\\AISeStatement\\ (del /q " + env.outputPath + "\\AISeStatement\\*)"
			def START_TIME_EN = getTime()
			echo START_TIME_EN
			
			//Pybot
			bat 'pybot -v ar_LANG:EN -v ar_BROWSER:gc -v ar_OS:Android -v ar_Mode:Online -v ar_TAKE_TIMEOUT:3000 -v ar_Tag:' + runner.get_tag() + ' -i login --nostatusrc ' + env.WORKSPACE + '\\SCGP_QATraceBack\\SCGP_QATraceBack_Prod\\Web\\WebPC\\SCGP_QATraceBack_Executor.txt'
			def END_TIME_EN = getTime()
			echo END_TIME_EN
			//Rebot
			bat 'rebot --outputdir '+ env.outputPath +'\\Web\\WebPC\\Login -o output_3BO_EN.xml -l log3BO_EN.html -r report3BO_EN.html  -R -N Online --nostatusrc '+ env.outputPath +'\\AISeStatement\\output*_EN_3BO.xml'
			bat 'rebot --outputdir '+ env.outputPath +'\\Web\\WebPC\\Login -o outputEN.xml -N EN -l log.html -r report.html --nostatusrc --starttime "' + START_TIME_EN + '" --endtime "' + END_TIME_EN + '" '+ env.outputPath +'\\Web\\WebPC\\Login\\output_EN_Online.xml '
			def START_TIME_TH = getTime()
			echo START_TIME_TH
			
			//Pybot
			bat 'pybot -v ar_LANG:TH -v ar_BROWSER:gc -v ar_OS:Android -v ar_Mode:Online -v ar_TAKE_TIMEOUT:3000 -v ar_Tag:' + runner.get_tag() + ' -i login --nostatusrc ' + env.WORKSPACE + '\\SCGP_QATraceBack\\SCGP_QATraceBack_Prod\\Web\\WebPC\\SCGP_QATraceBack_Executor.txt'
			def END_TIME_TH = getTime()
			echo END_TIME_TH
			//Rebot
			bat 'rebot --outputdir '+ env.outputPath +'\\Web\\WebPC\\Login -o output_3BO_TH.xml -l log3BO_TH.html -r report3BE_TH.html  -R -N Online --nostatusrc '+ env.outputPath +'\\Web\\WebPC\\Login\\output_TH_Online.xml '
			bat 'rebot --outputdir '+ env.outputPath +'\\Web\\WebPC\\Login -o outputTH.xml -N TH -l log.html -r report.html --nostatusrc --starttime "' + START_TIME_TH + '" --endtime "' + END_TIME_TH + '" '+ env.outputPath +'\\Web\\WebPC\\Login\\output_TH_Online.xml '
			
			//Merge all rebot
			bat 'rebot --nostatusrc --outputdir '+ env.outputPath +'\\AISeStatement -o output.xml -N AISeStatement ' + env.outputPath +'\\Web\\WebPC\\Login\\output_EN_Online.xml '+ env.outputPath +''\\Web\\WebPC\\Login\\output_TH_Online.xml '
			
			//Publish report
			build job: '(Z001)_SCGP_QATraceBack_Login_Prod', propagate: false
		}
	}
	else {echo 'not run'}
}

def run_all_report(runner){
	if(runner.AllReport && !runner.IsSmoke && !runner.IsSanity){
		build job: '(Z998)_SCGP_QATraceBack_AllReport', propagate: false
	}
	else {echo 'not run'}
}

def run_export_to_report_excel(runner){
	if(runner.ExportToReportExcel&& !runner.IsSmoke && !runner.IsSanity) {
		build job: '(Z999)_SCGP_QATraceBack_ExportToReportExcel', propagate: false
	}
	else {echo 'not run'}
}
