/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.land.bootstrap;
import net.hasor.core.AppContext;
import net.hasor.core.Environment;
import net.hasor.core.EventListener;
import net.hasor.core.Hasor;
import net.hasor.land.domain.WorkMode;
import net.hasor.land.election.ElectionService;
import net.hasor.land.election.ElectionServiceManager;
import net.hasor.land.node.Server;
import net.hasor.land.node.ServerNode;
import net.hasor.rsf.RsfApiBinder;
import net.hasor.rsf.RsfModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 启动入口
 * @version : 2016年10月12日
 * @author 赵永春 (zyc@hasor.net)
 */
public class LandModule implements RsfModule {
    protected static Logger logger = LoggerFactory.getLogger(LandModule.class);
    @Override
    public void loadModule(final RsfApiBinder apiBinder) throws Throwable {
        Environment env = apiBinder.getEnvironment();
        WorkMode workMode = env.getSettings().getEnum("hasor.land.workAt", WorkMode.class, WorkMode.None);
        if (WorkMode.None == workMode) {
            this.logger.warn("land workAt None mode, so land cannot be started.");
            return;
        }
        //
        // .注册Bean
        apiBinder.bindType(LandContext.class).asEagerSingleton();
        apiBinder.bindType(Server.class).to(ServerNode.class).asEagerSingleton();
        apiBinder.bindType(ElectionService.class).to(ElectionServiceManager.class).asEagerSingleton();
        //
        // .注册选举服务(隐藏的消息服务)
        apiBinder.rsfService(apiBinder.getBindInfo(ElectionService.class))//
                .asAloneThreadPool().asShadow().register();
        //
        Hasor.addStartListener(apiBinder.getEnvironment(), (EventListener<AppContext>) (event, eventData) -> {
            eventData.getInstance(ElectionService.class);
        });
    }
}