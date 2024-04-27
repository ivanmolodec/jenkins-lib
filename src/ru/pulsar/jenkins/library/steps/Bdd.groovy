package ru.pulsar.jenkins.library.steps

import ru.pulsar.jenkins.library.IStepExecutor
import ru.pulsar.jenkins.library.configuration.JobConfiguration
import ru.pulsar.jenkins.library.ioc.ContextRegistry
import ru.pulsar.jenkins.library.utils.Logger
import ru.pulsar.jenkins.library.utils.VRunner

import java.lang.reflect.Array

class Bdd implements Serializable {

    private final JobConfiguration config

    Bdd(JobConfiguration config) {
        this.config = config
    }

    def run() {
        IStepExecutor steps = ContextRegistry.getContext().getStepExecutor()

        Logger.printLocation()

        if (!config.stageFlags.bdd) {
            Logger.println("BDD step is disabled")
            return
        }

        List<String> logosConfig = ["LOGOS_CONFIG=$config.logosConfig"]
        steps.withEnv(logosConfig) {
            steps.installLocalDependencies()

            steps.createDir('build/out')
            def returnStatuses = []
            config.bddOptions.vrunnerSteps.each {
                Logger.println("Шаг запуска сценариев командой ${it}")
                String vrunnerPath = VRunner.getVRunnerPath()
                returnStatuses.add(VRunner.exec("$vrunnerPath ${it} --ibconnection \"/F./build/ib\"", true))
            }
            returnStatuses.each {
                Logger.println("${it}")
            }
            Logger.println(returnStatuses.size().toString())
            if (returnStatuses.contains(0) || returnStatuses.contains(2)) {
                Logger.println("Тестирование сценариев завершилось успешно")
            } else if (returnStatuses.contains(1)) {
                Logger.println("Тестирование сценариев завершилось, но часть фич/сценариев упала")
                //steps.unstable("Тестирование сценариев завершилось, но часть фич/сценариев упала")
            } else {
                Logger.println("Получен неожиданный/неверный результат работы. Возможно, работа 1С:Предприятие завершилась некорректно, или возникла ошибка при запуске")
                //steps.error()
            }
        }

        steps.stash('bdd-allure', 'build/out/allure/**', true)
        steps.stash('bdd-cucumber', 'build/out/cucumber/**', true)
    }
}
