package org.luncert.testdatahelper.component;

/**
 * @author Luncert
 */
public enum ProjectStatus {

  /**
   * Project's SCM url is invalid.
   */
  CloneFailed,

  /**
   * Failed to sync with remote.
   */
  SyncRemoteFailed,

  /**
   * Failed to get project branch info.
   */
  GetBranchInfoFailed,

  GetBranchInfoSucceed,

  /**
   * Failed to checkout the specified branch.
   */
  CheckoutFailed,

  /**
   * Failed to resolve project.
   */
  ResolveFailed,

  /**
   * Everything is done.
   */
  Ok
}
