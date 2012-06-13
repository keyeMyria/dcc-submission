package org.icgc.dcc.web;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.icgc.dcc.model.Release;
import org.icgc.dcc.service.NextRelease;
import org.icgc.dcc.service.ReleaseService;

import com.google.inject.Inject;

@Path("nextRelease")
public class NextReleaseResource {

  @Inject
  private ReleaseService releaseService;

  @GET
  public Response getNextRelease() {
    NextRelease nextRelease = releaseService.getNextRelease();
    return Response.ok(nextRelease.getRelease()).build();
  }

  @POST
  public Response release(Release nextRelease) {
    NextRelease oldRelease = releaseService.getNextRelease();
    NextRelease newRelease = oldRelease.release(nextRelease);

    return Response.ok(newRelease).build();
  }

  @GET
  @Path("queue")
  public Response getQueue() {
    List<String> projectIds = this.releaseService.getQueued();

    return Response.ok(projectIds).build();
  }

  @POST
  @Path("queue")
  public Response queue(List<String> projectKeys) {
    if(this.releaseService.queue(projectKeys)) {
      return Response.ok().build();
    } else {
      return Response.status(Status.BAD_REQUEST).build();
    }
  }

  @DELETE
  @Path("queue")
  public Response removeAllQueued() {
    this.releaseService.deleteQueuedRequest();

    return Response.ok().build();
  }

  @GET
  @Path("signed")
  public Response getSignedOff() {
    List<String> projectIds = this.releaseService.getSignedOff();

    return Response.ok(projectIds).build();
  }

  @POST
  @Path("signed")
  public Response signOff(List<String> projectKeys) {
    if(this.releaseService.signOff(projectKeys)) {
      return Response.ok().build();
    } else {
      return Response.status(Status.BAD_REQUEST).build();
    }
  }
}
