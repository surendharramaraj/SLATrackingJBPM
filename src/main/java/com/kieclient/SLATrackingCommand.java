package com.kieclient;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.core.timer.DateTimeUtils;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.jbpm.shared.services.impl.commands.QueryStringCommand;

import javax.persistence.EntityManagerFactory;

import org.kie.api.executor.Command;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.executor.Reoccurring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLATrackingCommand implements Command, Reoccurring {

    private static final Logger logger = LoggerFactory.getLogger(SLATrackingCommand.class);

    private long nextScheduleTimeAdd = 1 * 60 * 60 * 1000; // one hour in milliseconds

    @Override
    public Date getScheduleTime() {
        if (nextScheduleTimeAdd < 0) {
            return null;
        }

        long current = System.currentTimeMillis();

        Date nextSchedule = new Date(current + nextScheduleTimeAdd);
        logger.debug("Next schedule for job {} is set to {}", this.getClass().getSimpleName(), nextSchedule);

        return nextSchedule;
    }

    @Override
    public ExecutionResults execute(CommandContext ctx) throws Exception {
        ExecutionResults executionResults = new ExecutionResults();
        String emfName = (String) ctx.getData("EmfName");
        if (emfName == null) {
            emfName = "org.jbpm.domain";
        }
        // String singleRun = (String) ctx.getData("SingleRun");
        String singleRun = "singleRun";

        if ("true".equalsIgnoreCase(singleRun)) {
            // disable rescheduling
            this.nextScheduleTimeAdd = -1;
        }

        String nextRun = (String) ctx.getData("NextRun");
        if (nextRun != null) {
            nextScheduleTimeAdd = DateTimeUtils.parseDateAsDuration(nextRun);
        }

        // get hold of persistence and create instance of audit service
        EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate(emfName);

        // collect parameters
        String forDeployment = (String) ctx.getData("ForDeployment");

        // SLA Violations on process
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("now", new Date());
        StringBuilder lookupQuery = new StringBuilder();

        lookupQuery.append("select * from ProcessInstanceLog proc ");
        lookupQuery.append("where proc.status = 1 ");
        lookupQuery.append("and proc.sla_due_date < CURRENT_TIMESTAMP() ");
        lookupQuery.append("and proc.slacompliance = 3 GROUP BY proc.processinstanceid");

        if (forDeployment != null && !forDeployment.isEmpty()) {
            lookupQuery.append(" and log.externalId = :forDeployment");
            parameters.put("forDeployment", forDeployment);
        }

        TransactionalCommandService commandService = new TransactionalCommandService(emf);
        List<ProcessInstanceLog> nodeInstancesViolations = commandService
                .execute(new QueryStringCommand<List<ProcessInstanceLog>>(lookupQuery.toString(), parameters));
        logger.debug("Number of node instances with violated SLA {}", nodeInstancesViolations.size());

        return executionResults;

    }


    public void executeMethod(){
        ExecutionResults executionResults = new ExecutionResults();
        // String emfName = (String) ctx.getData("EmfName");
        String emfName = "java:jboss/datasources/ExampleDS";
        // if (emfName == null) {
        //     emfName = "org.jbpm.domain";
        // }
        // String singleRun = (String) ctx.getData("SingleRun");
        String singleRun = "singleRun";

        if ("true".equalsIgnoreCase(singleRun)) {
            // disable rescheduling
            this.nextScheduleTimeAdd = -1;
        }

        // String nextRun = (String) ctx.getData("NextRun");
        // String nextRun = "nextRun";

        // if (nextRun != null) {
        //     nextScheduleTimeAdd = DateTimeUtils.parseDateAsDuration(nextRun);
        // }

        // get hold of persistence and create instance of audit service
        EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate(emfName);

        // collect parameters
        // String forDeployment = (String) ctx.getData("ForDeployment");

        String forDeployment = "";
        // SLA Violations on process
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("now", new Date());
        StringBuilder lookupQuery = new StringBuilder();

        lookupQuery.append("select * from ProcessInstanceLog proc ");
        lookupQuery.append("where proc.status = 1 ");
        lookupQuery.append("and proc.sla_due_date < CURRENT_TIMESTAMP() ");
        lookupQuery.append("and proc.slacompliance = 3 GROUP BY proc.processinstanceid");

        if (forDeployment != null && !forDeployment.isEmpty()) {
            lookupQuery.append(" and log.externalId = :forDeployment");
            parameters.put("forDeployment", forDeployment);
        }

        TransactionalCommandService commandService = new TransactionalCommandService(emf);
        List<ProcessInstanceLog> nodeInstancesViolations = commandService
                .execute(new QueryStringCommand<List<ProcessInstanceLog>>(lookupQuery.toString(), parameters));
        logger.debug("Number of node instances with violated SLA {}", nodeInstancesViolations.size());

    }

}
