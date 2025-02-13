import { gql } from '@apollo/client';
import { BadSmell } from './data/BadSmell';
import { Project } from './data/Project';

export const fetchProjectQuery = gql`
  query getProjects {
    getProjects {
      projectName
      projectUrl
      commitHashes
      commits {
        analyzerStatuses {
          analyzerName
          commitHash
          localDateTime
          numberOfIssues
          status
        }
        commitHash
      }
    }
  }
`;
export const fetchAvailableRefactorings = gql`
  query getAvailableRefactorings {
    availableRefactorings {
      ruleId {
        id
      }
    }
  }
`;

export const fetchBadSmellsforHashQuery = gql`
  query getBadSmellsForHash($hash: String) {
    byCommitHash(commitHash: $hash) {
      identifier
      ruleID
      messageMarkdown
      snippet
      filePath
      position {
        startLine
      }
    }
  }
`;

export const addprojectQuery = gql`
  mutation addProject($projectName: String!, $projectUrl: String!) {
    addProject(projectName: $projectName, projectUrl: $projectUrl) {
      projectName
      projectUrl
    }
  }
`;
export const refactorQuery = gql`
  mutation refactor($badSmellIdentifier: [String]) {
    refactor(badSmellIdentifier: $badSmellIdentifier)
  }
`;
export const loginQuery = gql`
  query login($notNeeded: String) {
    login(notNeeded: $notNeeded)
  }
`;
export function filterDuplicates(params: Project[]) {
  return params;
}
export function filterDuplicateBadSmells(params: BadSmell[]) {
  if (params == null) {
    return [];
  }
  params = params.filter((badSmell) => {
    return badSmell.snippet != null;
  });
  const ids = params.map((o) => o.snippet);
  const filtered = params.filter(
    ({ snippet }, index) => !ids.includes(snippet, index + 1)
  );
  return filtered;
}

export const fetchProjectConfigQuery = gql`
  query getProjectConfig($projectUrl: String!) {
    getProjectConfig(projectUrl: $projectUrl) {
      projectUrl
      sourceFolder
    }
  }
`;
export const addProjectConfigQuery = gql`
  mutation addProjectConfig($projectConfig: ProjectConfig!) {
    addProjectConfig(projectConfig: $projectConfig) {
      projectUrl
      sourceFolder
    }
  }
`;

export const getGitHubCommitsQuery = gql`
  query getGitHubCommitsForProject($projectName: String!) {
    getGitHubCommitsForProject(projectName: $projectName) {
      analyzerStatuses {
        analyzerName
        commitHash
        localDateTime
        numberOfIssues
        status
      }
      commitHash
    }
  }
`;
