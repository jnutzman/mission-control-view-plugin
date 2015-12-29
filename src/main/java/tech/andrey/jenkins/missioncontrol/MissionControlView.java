package tech.andrey.jenkins.missioncontrol;

import hudson.Extension;
import hudson.model.*;
import hudson.security.Permission;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
@ExportedBean
public class MissionControlView extends View {
    private int getBuildsLimit;

    private String viewName;

    private int fontSize;

    private boolean useCondensedTables;

    private String statusButtonSize;

    @DataBoundConstructor
    public MissionControlView(String name, String viewName) {
        super(name);
        this.viewName = viewName;
        this.fontSize = 16;
        this.getBuildsLimit = 50;
        this.useCondensedTables = false;
        this.statusButtonSize = "default";
    }

    @Override
    public Collection<TopLevelItem> getItems() {
        return new ArrayList<TopLevelItem>();
    }

    public int getFontSize() {
        return fontSize;
    }

    public boolean isUseCondensedTables() {
        return useCondensedTables;
    }

    public String getTableStyle() {
        return useCondensedTables ? "table-condensed" : "";
    }

    public String getStatusButtonSize() {
        return statusButtonSize;
    }

    @Override
    protected void submit(StaplerRequest req) throws ServletException, IOException {
        JSONObject json = req.getSubmittedForm();
        this.fontSize = json.getInt("fontSize");
        this.useCondensedTables = json.getBoolean("useCondensedTables");
        this.statusButtonSize = json.getString("statusButtonSize");
        save();
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        return Jenkins.getInstance().doCreateItem(req, rsp);
    }

    @Override
    public boolean contains(TopLevelItem item) {
        return false;
    }

    @Override
    public boolean hasPermission(final Permission p) { return true; }

    /**
     * This descriptor class is required to configure the View Page
     */
    @Extension
    public static final class DescriptorImpl extends ViewDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.MissionControlView_DisplayName();
        }
    }

    public Api getApi() {
        return new Api(this);
    }

    @Exported(name="builds")
    public Collection<Build> getBuildHistory() {
        List<Item> items = Jenkins.getInstance().getAllItems();
        List<Job> jobs = new ArrayList<Job>();
        for (Item item : items) {
            jobs.addAll(item.getAllJobs());
        }
        RunList builds = new RunList(jobs).limit(this.getBuildsLimit);
        ArrayList<Build> l = new ArrayList<Build>();
        for (Object b : builds) {
            Run build = (Run)b;
            Result result = build.getResult();
            l.add(new Build(build.getParent().getName(),
                    build.getFullDisplayName(),
                    build.getNumber(),
                    build.getStartTimeInMillis(),
                    build.getDuration(),
                    build.getBuildStatusSummary().message,
                    result == null ? "building" : result.toString()));
        }
        return l;
    }

    @ExportedBean(defaultVisibility = 999)
    public class Build {
        @Exported
        public String jobName;
        @Exported
        public String buildName;
        @Exported
        public int number;
        @Exported
        public long startTime;
        @Exported
        public long duration;
        @Exported
        public String status;
        @Exported
        public String result;

        public Build(String jobName, String buildName, int number, long startTime, long duration, String status, String result) {
            this.jobName = jobName;
            this.buildName = buildName;
            this.number = number;
            this.startTime = startTime;
            this.duration = duration;
            this.status = status;
            this.result = result;
        }
    }
}