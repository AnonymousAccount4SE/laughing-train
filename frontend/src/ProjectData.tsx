import { gql} from '@apollo/client';
import { BadSmell } from './data/BadSmell';
import { Project } from './data/Project';





export const fetchProjectQuery = gql`
  query getProjects {
   getProjects {
   projectName
   projectUrl
   commitHashes
   }
}
`;

export const fetchBadSmellsforHashQuery = gql`
  query getBadSmellsForHash($hash: String) {
  byCommitHash(commitHash: $hash) {
    id
    ruleID
    messageMarkdown
    snippet
    filePath
    startLine
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
export const loginQuery = gql`
  query login($notNeeded : String) {
  login(notNeeded : $notNeeded)
}
`;
export function filterDuplicates(params:Project[]) {
  return params;
}
export function filterDuplicateBadSmells(params: BadSmell[]) {
  if (params == null) {
    return [];
  }
  params = params.filter((badSmell) => {
    return badSmell.snippet != null;
  });
  const ids = params.map(o => o.snippet)
  const filtered = params.filter(({ snippet }, index) => !ids.includes(snippet, index + 1));
  return filtered;
}

