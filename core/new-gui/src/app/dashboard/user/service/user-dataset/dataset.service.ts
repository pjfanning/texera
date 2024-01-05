import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { map } from "rxjs/operators";
import { NotificationService } from "../../../../common/service/notification/notification.service";
import { Dataset, DatasetVersion } from "../../../../common/type/dataset";
import { AppSettings } from "../../../../common/app-setting";
import { Observable } from "rxjs";
import { SearchFilterParameters, toQueryStrings } from "../../type/search-filter-parameters";
import { DashboardDataset } from "../../type/dashboard-dataset.interface";
import { FileUploadItem } from "../../type/dashboard-file.interface";
import { UserFileUploadService } from "../user-file/user-file-upload.service";
import {
  DatasetVersionFileTree,
  DatasetVersionFileTreeNode,
  parseFileTreeToNodes,
} from "../../../../common/type/datasetVersionFileTree";

export const DATASET_BASE_URL = "dataset";
export const DATASET_CREATE_URL = DATASET_BASE_URL + "/create";
export const DATASET_LIST_URL = DATASET_BASE_URL + "/list";
export const DATASET_SEARCH_URL = DATASET_BASE_URL + "/search";
export const DATASET_DELETE_URL = DATASET_BASE_URL + "/delete";

export const DATASET_VERSION_BASE_URL = "version";
export const DATASET_VERSION_RETRIEVE_LIST_URL = DATASET_VERSION_BASE_URL + "/list";
export const DATASET_VERSION_LATEST_URL = DATASET_VERSION_BASE_URL + "/latest";

@Injectable({
  providedIn: "root",
})
export class DatasetService {
  constructor(private http: HttpClient, private notificationService: NotificationService) {}

  public createDataset(
    dataset: Dataset,
    initialVersionName: string,
    filesToBeUploaded: FileUploadItem[]
  ): Observable<DashboardDataset> {
    const formData = new FormData();
    formData.append("datasetName", dataset.name);
    formData.append("datasetDescription", dataset.description);
    formData.append("isDatasetPublic", dataset.isPublic.toString());
    formData.append("initialVersionName", initialVersionName);

    filesToBeUploaded.forEach(file => {
      const sanitizedFilePath = UserFileUploadService.sanitizeFilePath(file.name);
      formData.append(`file:upload:${sanitizedFilePath}`, file.file);
    });

    return this.http.post<DashboardDataset>(`${AppSettings.getApiEndpoint()}/${DATASET_CREATE_URL}`, formData).pipe();
  }

  public getDataset(did: number): Observable<Dataset> {
    return this.http
      .get<DashboardDataset>(`${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}`)
      .pipe(map(datasetDashboard => datasetDashboard.dataset));
  }

  public retrieveDatasetVersionSingleFile(did: number, dvid: number, path: string): Observable<Blob> {
    const encodedPath = encodeURIComponent(path);
    return this.http.get(
      `${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/version/${dvid}/file?path=${encodedPath}`,
      { responseType: "blob" }
    );
  }

  public createDatasetVersion(
    did: number,
    newVersion: string,
    removedFilePaths: string[],
    filesToBeUploaded: FileUploadItem[]
  ): Observable<DatasetVersion> {
    const formData = new FormData();
    formData.append("versionName", newVersion);

    if (removedFilePaths.length > 0) {
      const removedFilesString = removedFilePaths.join(",");
      formData.append("file:remove", removedFilesString);
    }

    filesToBeUploaded.forEach(file => {
      const sanitizedFilePath = UserFileUploadService.sanitizeFilePath(file.name);
      formData.append(`file:upload:${sanitizedFilePath}`, file.file);
    });

    return this.http
      .post<{ datasetVersion: DatasetVersion; fileTree: DatasetVersionFileTree }>(
        `${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/version/create`,
        formData
      )
      .pipe(
        map(response => {
          response.datasetVersion.versionFileTreeNodes = parseFileTreeToNodes(response.fileTree);
          return response.datasetVersion;
        })
      );
  }

  /**
   * retrieve a list of versions of a dataset. The list is sorted so that the latest versions are at front.
   * @param did
   */
  public retrieveDatasetVersionList(did: number): Observable<DatasetVersion[]> {
    return this.http
      .get<{ versions: DatasetVersion[] }>(
        `${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/${DATASET_VERSION_RETRIEVE_LIST_URL}`
      )
      .pipe(map(response => response.versions));
  }

  /**
   * retrieve the latest version of a dataset.
   * @param did
   */
  public retrieveDatasetLatestVersion(did: number): Observable<DatasetVersion> {
    return this.http
      .get<{ datasetVersion: DatasetVersion; fileTree: DatasetVersionFileTree }>(
        `${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/${DATASET_VERSION_LATEST_URL}`
      )
      .pipe(
        map(response => {
          response.datasetVersion.versionFileTreeNodes = parseFileTreeToNodes(response.fileTree);
          return response.datasetVersion;
        })
      );
  }

  /**
   * retrieve a list of nodes that represent the files in the version
   * @param did
   * @param dvid
   */
  public retrieveDatasetVersionFileTree(did: number, dvid: number): Observable<DatasetVersionFileTreeNode[]> {
    return this.http
      .get<{ fileTree: DatasetVersionFileTree }>(
        `${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/${DATASET_VERSION_BASE_URL}/${dvid}/fileTree`
      )
      .pipe(map(response => parseFileTreeToNodes(response.fileTree)));
  }

  public deleteDatasets(dids: number[]): Observable<Response> {
    return this.http.post<Response>(`${AppSettings.getApiEndpoint()}/${DATASET_DELETE_URL}`, {
      dids: dids,
    });
  }
}
