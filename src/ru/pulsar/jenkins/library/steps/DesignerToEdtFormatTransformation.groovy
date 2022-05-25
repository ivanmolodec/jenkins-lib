package ru.pulsar.jenkins.library.steps

import ru.pulsar.jenkins.library.IStepExecutor
import ru.pulsar.jenkins.library.configuration.JobConfiguration
import ru.pulsar.jenkins.library.ioc.ContextRegistry
import ru.pulsar.jenkins.library.utils.Constants
import ru.pulsar.jenkins.library.utils.EDT
import ru.pulsar.jenkins.library.utils.FileUtils
import ru.pulsar.jenkins.library.utils.Logger

import java.nio.file.Paths

class DesignerToEdtFormatTransformation implements Serializable {

    public static final String PROJECT_NAME = 'temp'
    public static final String WORKSPACE = 'build/edt-workspace'
    public static final String SETTINGS = '.settings'
    public static final String WORKSPACE_ZIP = 'build/edt-workspace.zip'
    public static final String WORKSPACE_ZIP_STASH = 'edt-workspace-zip'

    private final JobConfiguration config;

    DesignerToEdtFormatTransformation(JobConfiguration config) {
        this.config = config
    }

    def run() {
        IStepExecutor steps = ContextRegistry.getContext().getStepExecutor()

        Logger.printLocation()

        if (!config.stageFlags.edtValidate) {
            Logger.println("EDT validate step is disabled. No transform is needed.")
            return
        }

        def env = steps.env();

        def workspaceDir = "$env.WORKSPACE/$WORKSPACE"
        def configurationRoot = new File(env.WORKSPACE, config.srcDir).getAbsolutePath()
        def edtVersionForRing = EDT.ringModule(config)
        def options = config.edtValidateOptions

        steps.deleteDir(workspaceDir)

        Logger.println("Конвертация исходников из формата конфигуратора в формат EDT")

        def ringCommand = "ring $edtVersionForRing workspace import --configuration-files \"$configurationRoot\" --project-name $PROJECT_NAME --workspace-location \"$workspaceDir\""

        def ringOpts = [Constants.DEFAULT_RING_OPTS]
        steps.withEnv(ringOpts) {
            steps.cmd(ringCommand)
        }

        if (!options.managedEnvironmentsFile.empty && steps.fileExists(options.managedEnvironmentsFile)) {
            String managedEnvironmentsFile = FileUtils.getFilePath("$env.WORKSPACE/$options.managedEnvironmentsFile").readToString()
            String copied = Paths.get("$workspaceDir/$PROJECT_NAME/$SETTINGS").toAbsolutePath()
            steps.fileOperations([steps.fileCopyOperation(copied, '', managedEnvironmentsFile, false, false, '', '')])
        }

        steps.zip(WORKSPACE, WORKSPACE_ZIP)
        steps.stash(WORKSPACE_ZIP_STASH, WORKSPACE_ZIP)
    }

}
