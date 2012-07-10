package org.icgc.dcc.release;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.icgc.dcc.core.morphia.BaseMorphiaService;
import org.icgc.dcc.release.model.QRelease;
import org.icgc.dcc.release.model.Release;
import org.icgc.dcc.release.model.ReleaseState;
import org.icgc.dcc.release.model.Submission;
import org.icgc.dcc.release.model.SubmissionState;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.mysema.query.mongodb.MongodbQuery;

public class ReleaseService extends BaseMorphiaService<Release> {

  @Inject
  public ReleaseService(Morphia morphia, Datastore datastore) {
    super(morphia, datastore, QRelease.release);
    registerModelClasses(Release.class);
  }

  public void createInitialRelease(Release initRelease) {
    // create the first release
    datastore().save(initRelease);
  }

  public NextRelease getNextRelease() throws IllegalReleaseStateException {
    Release nextRelease = this.query().where(QRelease.release.state.eq(ReleaseState.OPENED)).singleResult();
    return new NextRelease(nextRelease, datastore());
  }

  public ReleaseState nextReleaseState() throws IllegalReleaseStateException {
    return getNextRelease().getRelease().getState();
  }

  public List<HasRelease> list() {
    List<HasRelease> list = new ArrayList<HasRelease>();

    MongodbQuery<Release> query = this.query();

    for(Release release : query.list()) {
      list.add(new BaseRelease(release));
    }

    return list;
  }

  public List<CompletedRelease> getCompletedReleases() throws IllegalReleaseStateException {
    List<CompletedRelease> completedReleases = new ArrayList<CompletedRelease>();

    MongodbQuery<Release> query = this.where(QRelease.release.state.eq(ReleaseState.COMPLETED));

    for(Release release : query.list()) {
      completedReleases.add(new CompletedRelease(release));
    }

    return completedReleases;
  }

  public Submission getSubmission(String releaseName, String projectKey) {
    Release release = this.where(QRelease.release.name.eq(releaseName)).uniqueResult();
    checkArgument(release != null);

    Submission result = null;
    for(Submission submission : release.getSubmissions()) {
      if(submission.getProjectKey().equals(projectKey)) {
        result = submission;
        break;
      }
    }

    checkState(result != null);

    return result;
  }

  public boolean queue(List<String> projectKeys) {
    this.getNextRelease().release.enqueue(projectKeys);
    return this.setState(projectKeys, SubmissionState.QUEUED);
  }

  public Optional<String> dequeue(String projectKey, boolean valid) {
    Optional<String> dequeued = Optional.<String> absent();
    List<String> projectKeys = this.getQueued();
    if(null != projectKeys && projectKeys.isEmpty() == false) {
      String first = projectKeys.get(0);
      if(null != first && first.equals(projectKey)) {
        dequeued = Optional.<String> of(this.getNextRelease().release.dequeue());

        this.setState(dequeued.get(), valid ? SubmissionState.VALID : SubmissionState.INVALID);
        // TODO: actually update the queue in db (throughough class)
      }
    }
    return dequeued;
  }

  public List<String> getQueued() {
    return ImmutableList.copyOf(this.getSubmission(SubmissionState.QUEUED));
  }

  public void deleteQueuedRequest() {
    List<String> projectKeys = this.getQueued();

    this.setState(projectKeys, SubmissionState.NOT_VALIDATED);
    this.getNextRelease().release.emptyQueue();
  }

  public List<String> getSignedOff() {
    return this.getSubmission(SubmissionState.SIGNED_OFF);
  }

  public boolean signOff(List<String> projectKeys) {
    return this.setState(projectKeys, SubmissionState.SIGNED_OFF);
  }

  private List<String> getSubmission(SubmissionState state) {
    List<String> result = new ArrayList<String>();
    for(Submission submission : this.getNextRelease().getRelease().getSubmissions()) {
      if(submission.getState().equals(state)) {
        result.add(submission.getProjectKey());
      }
    }
    return result;
  }

  private boolean setState(String projectKey, SubmissionState state) {
    return setState(Arrays.asList(projectKey), state);
  }

  private boolean setState(List<String> projectKeys, SubmissionState state) {
    UpdateOperations<Release> ops;
    Query<Release> updateQuery;

    checkArgument(projectKeys != null);

    ops = datastore().createUpdateOperations(Release.class).disableValidation().set("submissions.$.state", state);
    updateQuery =
        datastore().createQuery(Release.class).filter("name =", this.getNextRelease().getRelease().getName())
            .filter("submissions.projectKey in", projectKeys);
    datastore().update(updateQuery, ops);

    return true;
  }
}
