package com.conke.register

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.impldep.com.google.api.client.util.ArrayMap

/**
 * 自动注册插件入口
 * @author billy.qi
 * @since 17/3/14 17:35
 */
public class RegisterPlugin implements Plugin<Project> {

    public static final String EXT_NAME = 'autoregister'

    @Override
    public void apply(Project project) {
        /**
         * 注册transform接口
         */
        def isApp = project.plugins.hasPlugin(AppPlugin)
        project.extensions.create(EXT_NAME, AutoRegisterConfig)
        if (isApp) {
            println 'project(' + project.name + ') apply auto-register plugin'
            def android = project.extensions.getByType(AppExtension)
            def transformImpl = new RegisterTransform(project)
            android.registerTransform(transformImpl)
            project.afterEvaluate {
                init(project, transformImpl)//此处要先于transformImpl.transform方法执行
            }
        }
    }

    static void init(Project project, RegisterTransform transformImpl) {
        AutoRegisterConfig config = project.extensions.findByName(EXT_NAME) as AutoRegisterConfig
        if(config == null){
            config = new  AutoRegisterConfig()
            Map<String, Object> startup = new ArrayMap()
            startup.add(AutoRegisterConfig.INTERFACE_NAME,"com.cnoke.startup.application.IApplication")
            startup.add(AutoRegisterConfig.INSERT_TO_CLASS_NAME,"com.cnoke.startup.FinalAppRegister")
            startup.add(AutoRegisterConfig.INSERT_TO_METHOD_NAME,"init")
            startup.add(AutoRegisterConfig.REGISTER_CLASS_NAME,"com.cnoke.startup.FinalAppRegister")
            startup.add(AutoRegisterConfig.REGISTER_METHOD_NAME,"register")
            startup.add(AutoRegisterConfig.IS_INSTANCE,true)
            config.registerInfo.add(startup)
        }
        config.project = project
        config.convertConfig()
        transformImpl.config = config
    }

}
