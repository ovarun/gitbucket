package gitbucket.core.controller.api
import gitbucket.core.api.{ApiObject, ApiRef, JsonFormat}
import gitbucket.core.controller.ControllerBase
import gitbucket.core.util.Directory.getRepositoryDir
import gitbucket.core.util.ReferrerAuthenticator
import gitbucket.core.util.Implicits._
import org.eclipse.jgit.api.Git
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._
import scala.util.Using

trait ApiGitReferenceControllerBase extends ControllerBase {
  self: ReferrerAuthenticator =>

  private val logger = LoggerFactory.getLogger(classOf[ApiGitReferenceControllerBase])

  /*
   * i. Get a reference
   * https://developer.github.com/v3/git/refs/#get-a-reference
   */
  get("/api/v3/repos/:owner/:repository/git/ref/*")(referrersOnly { repository =>
    getRef()
  })

  // Some versions of GHE support this path
  get("/api/v3/repos/:owner/:repository/git/refs/*")(referrersOnly { repository =>
    logger.warn("git/refs/ endpoint may not be compatible with GitHub API v3. Consider using git/ref/ endpoint instead")
    getRef()
  })

  private def getRef() = {
    val revstr = multiParams("splat").head
    Using.resource(Git.open(getRepositoryDir(params("owner"), params("repository")))) { git =>
      val ref = git.getRepository().findRef(revstr)

      if (ref != null) {
        val sha = ref.getObjectId().name()
        JsonFormat(ApiRef(revstr, ApiObject(sha)))

      } else {
        val refs = git
          .getRepository()
          .getRefDatabase()
          .getRefsByPrefix("refs/")
          .asScala

        JsonFormat(refs.map { ref =>
          val sha = ref.getObjectId().name()
          ApiRef(revstr, ApiObject(sha))
        })
      }
    }
  }

  /*
   * ii. Get all references
   * https://developer.github.com/v3/git/refs/#get-all-references
   */

  /*
   * iii. Create a reference
   * https://developer.github.com/v3/git/refs/#create-a-reference
   */

  /*
   * iv. Update a reference
   * https://developer.github.com/v3/git/refs/#update-a-reference
   */

  /*
 * v. Delete a reference
 * https://developer.github.com/v3/git/refs/#delete-a-reference
 */
}
