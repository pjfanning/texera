import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {NotificationService} from "../../../../common/service/notification/notification.service";
import {Dataset} from "../../../../common/type/dataset";
import {AppSettings} from "../../../../common/app-setting";
import {WORKFLOW_PERSIST_URL} from "../../../../common/service/workflow-persist/workflow-persist.service";
import {Observable} from "rxjs";


export const DATASET_BASE_URL = "dataset";
export const DATASET_PERSIST_URL = DATASET_BASE_URL + "/persist";
export const DATASET_LIST_URL = DATASET_BASE_URL + "/list";
export const DATASET_SEARCH_URL = DATASET_BASE_URL + "/search";
export const DATASET_DELETE_URL = DATASET_BASE_URL + "/delete";

@Injectable({
  providedIn: "root",
})
export class DatasetService {
  constructor(private http: HttpClient, private notificationService: NotificationService) {}

  public persistDataset(dataset: Dataset): Observable<Dataset> {
    return this.http
      .post<Dataset>(`${AppSettings.getApiEndpoint()}/${DATASET_PERSIST_URL}`, {
        name: dataset.name,
        did: dataset.did
    }).pipe()
  }
}
