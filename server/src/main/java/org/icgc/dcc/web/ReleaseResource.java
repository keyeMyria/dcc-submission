package org.icgc.dcc.web;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.icgc.dcc.model.QRelease;
import org.icgc.dcc.model.QSubmission;
import org.icgc.dcc.model.Release;
import org.icgc.dcc.service.ReleaseService;

import com.google.inject.Inject;

@Path("releases")
public class ReleaseResource {

  @Inject
  private ReleaseService releaseService;

  @GET
  public Response getResources() {
    List<Release> releases = releaseService.query().list();

    return Response.ok(releases).build();
  }

  @GET
  @Path("{name}")
  public Response getReleaseByName(@PathParam("name") String name) {
    Release release = releaseService.where(QRelease.release.name.eq(name)).singleResult();
    if(release == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok(release).build();
  }

  @PUT
  @Path("{name}")
  public Response updateRelease(@PathParam("name") String name, Release release) {
    checkArgument(release != null);

    if(this.releaseService.where(QRelease.release.name.eq(release.getName())).list().isEmpty()) {
      this.releaseService.createInitialRelease(release);
    }
    // for now nothing is allowed to change
    /*
     * UpdateOperations<Release> ops =
     * this.releaseService.getDatastore().createUpdateOperations(Release.class).set("state", release.getState());
     * 
     * Query<Release> updateQuery =
     * this.releaseService.getDatastore().createQuery(Release.class).field("name").equal(name);
     * 
     * this.releaseService.getDatastore().update(updateQuery, ops);
     */
    return Response.ok(release).build();
  }

  @GET
  @Path("{name}/submissions/{accessionId}")
  public Response getSubmission(@PathParam("name") String name, @PathParam("accessionId") String accessionId) {
    Release release =
        releaseService.query()
            .where(QRelease.release.name.eq(name), QSubmission.submission.project.accessionId.eq(accessionId))
            .uniqueResult();

    return Response.ok(release.getSubmissions()).build();
  }
}
