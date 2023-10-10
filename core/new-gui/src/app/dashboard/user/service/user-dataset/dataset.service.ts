import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {NotificationService} from "../../../../common/service/notification/notification.service";
import {Dataset} from "../../../../common/type/dataset";
import {AppSettings} from "../../../../common/app-setting";
import {WORKFLOW_PERSIST_URL} from "../../../../common/service/workflow-persist/workflow-persist.service";
import {Observable} from "rxjs";
import {SearchFilterParameters, toQueryStrings} from "../../type/search-filter-parameters";
import {DashboardDataset} from "../../type/dashboard-dataset.interface";


export const DATASET_BASE_URL = "dataset";
export const DATASET_CREATE_URL = DATASET_BASE_URL + "/create";
export const DATASET_LIST_URL = DATASET_BASE_URL + "/list";
export const DATASET_SEARCH_URL = DATASET_BASE_URL + "/search";
export const DATASET_DELETE_URL = DATASET_BASE_URL + "/delete";

@Injectable({
  providedIn: "root",
})
export class DatasetService {
  constructor(private http: HttpClient, private notificationService: NotificationService) {}

  public createDataset(dataset: Dataset): Observable<Dataset> {
    return this.http
      .post<Dataset>(`${AppSettings.getApiEndpoint()}/${DATASET_CREATE_URL}`, {
        name: dataset.name,
        did: dataset.did
    }).pipe()
  }

  public retrieveDataset(did: number): Observable<Dataset> {
    return this.http
      .get<Dataset>(`${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}`)
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
