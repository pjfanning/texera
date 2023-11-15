import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import { map } from 'rxjs/operators';
import {NotificationService} from "../../../../common/service/notification/notification.service";
import {Dataset} from "../../../../common/type/dataset";
import {AppSettings} from "../../../../common/app-setting";
import {Observable} from "rxjs";
import {SearchFilterParameters, toQueryStrings} from "../../type/search-filter-parameters";
import {DashboardDataset} from "../../type/dashboard-dataset.interface";
import {DatasetVersionHierarchy, DatasetVersionHierarchyNode, parseHierarchyToNodes} from "../../../../common/type/datasetVersion";


export const DATASET_BASE_URL = "dataset";
export const DATASET_CREATE_URL = DATASET_BASE_URL + "/create";
export const DATASET_LIST_URL = DATASET_BASE_URL + "/list";
export const DATASET_SEARCH_URL = DATASET_BASE_URL + "/search";
export const DATASET_DELETE_URL = DATASET_BASE_URL + "/delete";

export const DATASET_VERSION_BASE_URL = "version";
export const DATASET_VERSION_LIST_URL = DATASET_VERSION_BASE_URL + "/list";



@Injectable({
  providedIn: "root",
})
export class DatasetService {
  constructor(private http: HttpClient, private notificationService: NotificationService) {}

  public createDataset(dataset: Dataset): Observable<DashboardDataset> {
    return this.http
      .post<DashboardDataset>(`${AppSettings.getApiEndpoint()}/${DATASET_CREATE_URL}`, {
        name: dataset.name,
        isPublic: dataset.isPublic,
        description: dataset.description,
    }).pipe()
  }

  public getDataset(did: number): Observable<Dataset> {
    return this.http.get<DashboardDataset>(`${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}`)
    .pipe(
      map(datasetDashboard => datasetDashboard.dataset)
    );
  }

  public inspectDatasetSingleFile(did: number, version: string, path: string): Observable<Blob> {
    return this.http.get(`${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/version/${version}/file?path=${path}`, {responseType: 'blob'});
  }

  public createDatasetVersion(
    did: number,
    baseVersion: string | null,
    newVersion: string,
    remove: string | null,
    files: File[]
  ): Observable<any> {
    const formData = new FormData();

    if (baseVersion !== null) {
      formData.append('baseVersion', baseVersion);
    }
    formData.append('version', newVersion);

    if (remove !== null) {
      formData.append('remove', remove);
    }

    files.forEach(file => {
      const path = file['webkitRelativePath'] || file.name;
      formData.append(path, file);
    });
    console.log("success");
    return this.http.post(`${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/version/create`, formData);
  }

  /**
   * retrieve a list of version name of a dataset. The list is sorted so that the latest versions are at front.
   * @param did
   */
  public retrieveDatasetVersionList(did: number): Observable<string[]> {
    return this.http
      .get<{ versions: string[]}>(`${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/${DATASET_VERSION_LIST_URL}`)
      .pipe(
        map(response => response.versions)
      )
  }

  /**
   * retrieve a list of nodes that represent the files in the version
   * @param did
   * @param version
   */
  public retrieveDatasetVersionFileHierarchy(did: number, version: string = ""): Observable<DatasetVersionHierarchyNode[]> {
    return this.http
      .get<{ hierarchy: DatasetVersionHierarchy }>(`${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/${DATASET_VERSION_BASE_URL}/${version}/hierarchy`)
      .pipe(
        map(response => response.hierarchy),
        map(parseHierarchyToNodes)  // Convert the DatasetVersionHierarchy to DatasetVersionHierarchyNode[]
      );
  }


  public retrieveDatasetsBySessionUser(): Observable<DashboardDataset[]> {
    return this.http
      .get<DashboardDataset[]>(`${AppSettings.getApiEndpoint()}/${DATASET_LIST_URL}`)
  }

  public searchDatasets(keywords: string[], params: SearchFilterParameters): Observable<DashboardDataset[]> {
    return this.http.get<DashboardDataset[]>(`${AppSettings.getApiEndpoint()}/${DATASET_SEARCH_URL}?${toQueryStrings(keywords, params)}`)
  }

  public deleteDatasets(dids: number[]): Observable<Response> {
    return this.http.post<Response>(`${AppSettings.getApiEndpoint()}/${DATASET_DELETE_URL}`, {
      dids: dids
    })
  }
}
