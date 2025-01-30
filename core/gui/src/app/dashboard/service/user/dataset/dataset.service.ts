import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { map } from "rxjs/operators";
import { Dataset, DatasetVersion } from "../../../../common/type/dataset";
import { AppSettings } from "../../../../common/app-setting";
import { Observable } from "rxjs";
import { DashboardDataset } from "../../../type/dashboard-dataset.interface";
import { FileUploadItem } from "../../../type/dashboard-file.interface";
import { DatasetFileNode } from "../../../../common/type/datasetVersionFileTree";
import { RepositoriesService, RepositoryCreation, Repository, CommitsService } from "lakefs"
import { S3Client, 
  PutObjectCommand, 
  PutObjectCommandOutput, 
  CreateMultipartUploadCommand, 
  UploadPartCommand, 
  CompleteMultipartUploadCommand, 
  AbortMultipartUploadCommand } from "@aws-sdk/client-s3"
import { defaultEnvironment } from "src/environments/environment.default";

export const DATASET_BASE_URL = "dataset";
export const DATASET_CREATE_URL = DATASET_BASE_URL + "/create";
export const DATASET_UPDATE_BASE_URL = DATASET_BASE_URL + "/update";
export const DATASET_UPDATE_NAME_URL = DATASET_UPDATE_BASE_URL + "/name";
export const DATASET_UPDATE_DESCRIPTION_URL = DATASET_UPDATE_BASE_URL + "/description";
export const DATASET_UPDATE_PUBLICITY_URL = "update/publicity";
export const DATASET_LIST_URL = DATASET_BASE_URL + "/list";
export const DATASET_SEARCH_URL = DATASET_BASE_URL + "/search";
export const DATASET_DELETE_URL = DATASET_BASE_URL + "/delete";

export const DATASET_VERSION_BASE_URL = "version";
export const DATASET_VERSION_RETRIEVE_LIST_URL = DATASET_VERSION_BASE_URL + "/list";
export const DATASET_VERSION_LATEST_URL = DATASET_VERSION_BASE_URL + "/latest";
export const DEFAULT_DATASET_NAME = "Untitled dataset";
export const DATASET_PUBLIC_VERSION_BASE_URL = "publicVersion";
export const DATASET_PUBLIC_VERSION_RETRIEVE_LIST_URL = DATASET_PUBLIC_VERSION_BASE_URL + "/list";
export const DATASET_GET_OWNERS_URL = DATASET_BASE_URL + "/datasetUserAccess";

const LAKEFS_ACCESS_KEY = "AKIAIOSFOLKFSSAMPLES"
const LAKEFS_SECRET_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"

const CHUNK_SIZE = 5 * 1024 * 1024; // 5MB per part (AWS & LakeFS minimum)

@Injectable({
  providedIn: "root",
})
export class DatasetService {
  private s3ClientLakefs: S3Client;

  constructor(private http: HttpClient, 
    private repositoriesService: RepositoriesService,
    private commitsService: CommitsService
  ) {
    this.repositoriesService.configuration.username = LAKEFS_ACCESS_KEY;
    this.repositoriesService.configuration.password = LAKEFS_SECRET_KEY;

    this.commitsService.configuration.username = LAKEFS_ACCESS_KEY;
    this.commitsService.configuration.password = LAKEFS_SECRET_KEY;

    this.s3ClientLakefs = new S3Client({
      region: 'us-east-2',
      endpoint: 'http://localhost:4200/s3',
      credentials: {
        accessKeyId: LAKEFS_ACCESS_KEY,
        secretAccessKey: LAKEFS_SECRET_KEY
      },
      forcePathStyle: true,
      requestChecksumCalculation: "WHEN_REQUIRED"
    });
  }

  public createDatasetLakefs(
    dataset: Dataset,
    initialVersionName: string,
    filesToBeUploaded: FileUploadItem[]
  ): void {

    const repositoryData: RepositoryCreation = {
      name: dataset.name,
      storage_namespace: `s3://${defaultEnvironment.s3BucketName}/${dataset.name}`,
      default_branch: 'main'
    };

    this.repositoriesService.createRepository(repositoryData, false).subscribe({
      next: (repository) => {
        const uploadPromises = filesToBeUploaded.map(file =>
          this.uploadFileS3(dataset.name, "main", file.name, file.file)
        );
  
        Promise.all(uploadPromises)
          .then(() => {
            this.createCommitLakefs(dataset);
          })
          .catch(error => {
            console.error("Error uploading files:", error);
          });
      },
      error: (err) => {
        console.error('Error creating repository:', err);
      }
    });
  }

  private createCommitLakefs(
    dataset: Dataset,
  ) {
    const commitParams = {
      message: dataset.description,
    }

    this.commitsService
      .commit(commitParams, dataset.name, "main")
      .subscribe({
        next: (commit) => {
          console.log("Commit created successfully:", commit);
        },
        error: (err) => {
          console.error("Error creating commit:", err);
        }
      })
  }

  private async uploadFileS3(
    repo: string,
    branch: string,
    key: string, 
    file: File
  ): Promise<void> {
    if (file.size > CHUNK_SIZE) {
      // Use multipart upload for large files
      await this.uploadFileMultipartS3(repo, branch, key, file);
    } else {
      // Use simple put object for smaller files
      const commandParams = {
        Bucket: repo,
        Key: `${branch}/${key}`,
        Body: file,
        ContentType: file.type,
      };
  
      const command = new PutObjectCommand(commandParams);
  
      return this.s3ClientLakefs.send(command)
        .then((data) => {
          console.log(`File ${key} uploaded successfully:`, data);
        })
        .catch((error) => {
          console.error(`Error uploading ${key} to LakeFS:`, error);
          throw error;
        });
    }
  }

  private async uploadFileMultipartS3(
    repo: string,
    branch: string,
    key: string,
    file: File
  ): Promise<void> {
    const objectKey = `${branch}/${key}`;
    const partCount = Math.ceil(file.size / CHUNK_SIZE);
    let uploadId: string | undefined;
  
    try {
      const createUploadResponse = await this.s3ClientLakefs.send(
        new CreateMultipartUploadCommand({
          Bucket: repo,
          Key: objectKey,
          ContentType: file.type,
        })
      );
  
      uploadId = createUploadResponse.UploadId;
      if (!uploadId) throw new Error("Failed to initiate multipart upload");
  
      console.log(`Started multipart upload for ${key} with UploadId: ${uploadId}`);
  
      // Step 2: Upload Parts
      const uploadPromises = [];
      const uploadedParts: { PartNumber: number; ETag: string | undefined }[] = [];
  
      for (let i = 0; i < partCount; i++) {
        const start = i * CHUNK_SIZE;
        const end = Math.min(start + CHUNK_SIZE, file.size);
        const chunk = file.slice(start, end);
  
        uploadPromises.push(
          (async () => {
            const uploadPartResponse = await this.s3ClientLakefs.send(
              new UploadPartCommand({
                Bucket: repo,
                Key: objectKey,
                UploadId: uploadId,
                PartNumber: i + 1,
                Body: chunk,
              })
            );
  
            console.log(`Uploaded part ${i + 1} of ${partCount}`);
  
            uploadedParts.push({
              PartNumber: i + 1,
              ETag: uploadPartResponse.ETag,
            });
          })()
        );
      }
  
      await Promise.all(uploadPromises);
  
      // Step 3: Complete Multipart Upload
      await this.s3ClientLakefs.send(
        new CompleteMultipartUploadCommand({
          Bucket: repo,
          Key: objectKey,
          UploadId: uploadId,
          MultipartUpload: { Parts: uploadedParts.sort((a, b) => a.PartNumber - b.PartNumber) },
        })
      );
  
      console.log(`Multipart upload for ${key} completed successfully!`);
    } catch (error) {
      console.error(`Multipart upload failed for ${key}`, error);

      if (uploadId) {
        await this.s3ClientLakefs.send(
          new AbortMultipartUploadCommand({
            Bucket: repo,
            Key: objectKey,
            UploadId: uploadId,
          })
        );
  
        console.error(`Upload aborted for ${key}`);
      }
    }
  }

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
      formData.append(`file:upload:${file.name}`, file.file);
    });

    this.createDatasetLakefs(dataset, initialVersionName, filesToBeUploaded);

    return this.http.post<DashboardDataset>(`${AppSettings.getApiEndpoint()}/${DATASET_CREATE_URL}`, formData);
  }

  public getDataset(did: number, isLogin: boolean = true): Observable<DashboardDataset> {
    const apiUrl = isLogin
      ? `${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}`
      : `${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/public/${did}`;
    return this.http.get<DashboardDataset>(apiUrl);
  }

  public retrieveDatasetVersionSingleFile(path: string): Observable<Blob> {
    const encodedPath = encodeURIComponent(path);
    return this.http.get(`${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/file?path=${encodedPath}`, {
      responseType: "blob",
    });
  }

  /**
   * Retrieves a zip file of a dataset or a specific path within a dataset.
   * @param options An object containing optional parameters:
   *   - path: A string representing a specific file or directory path within the dataset
   *   - did: A number representing the dataset ID
   * @returns An Observable that emits a Blob containing the zip file
   */
  public retrieveDatasetZip(options: { did: number; dvid?: number }): Observable<Blob> {
    let params = new HttpParams();
    params = params.set("did", options.did.toString());
    if (options.dvid) {
      params = params.set("dvid", options.dvid.toString());
    }

    return this.http.get(`${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/version-zip`, {
      params,
      responseType: "blob",
    });
  }

  public retrieveAccessibleDatasets(): Observable<DashboardDataset[]> {
    return this.http.get<DashboardDataset[]>(`${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}`);
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
      const removedFilesString = JSON.stringify(removedFilePaths);
      formData.append("file:remove", removedFilesString);
    }

    filesToBeUploaded.forEach(file => {
      formData.append(`file:upload:${file.name}`, file.file);
    });

    return this.http
      .post<{
        datasetVersion: DatasetVersion;
        fileNodes: DatasetFileNode[];
      }>(`${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/version/create`, formData)
      .pipe(
        map(response => {
          response.datasetVersion.fileNodes = response.fileNodes;
          return response.datasetVersion;
        })
      );
  }

  /**
   * retrieve a list of versions of a dataset. The list is sorted so that the latest versions are at front.
   * @param did
   * @param isLogin
   */
  public retrieveDatasetVersionList(did: number, isLogin: boolean = true): Observable<DatasetVersion[]> {
    const apiEndPont = isLogin
      ? `${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/${DATASET_VERSION_RETRIEVE_LIST_URL}`
      : `${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/${DATASET_PUBLIC_VERSION_RETRIEVE_LIST_URL}`;
    return this.http.get<DatasetVersion[]>(apiEndPont);
  }

  /**
   * retrieve the latest version of a dataset.
   * @param did
   */
  public retrieveDatasetLatestVersion(did: number): Observable<DatasetVersion> {
    return this.http
      .get<{
        datasetVersion: DatasetVersion;
        fileNodes: DatasetFileNode[];
      }>(`${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/${DATASET_VERSION_LATEST_URL}`)
      .pipe(
        map(response => {
          response.datasetVersion.fileNodes = response.fileNodes;
          return response.datasetVersion;
        })
      );
  }

  /**
   * retrieve a list of nodes that represent the files in the version
   * @param did
   * @param dvid
   * @param isLogin
   */
  public retrieveDatasetVersionFileTree(
    did: number,
    dvid: number,
    isLogin: boolean = true
  ): Observable<{ fileNodes: DatasetFileNode[]; size: number }> {
    const apiUrl = isLogin
      ? `${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/${DATASET_VERSION_BASE_URL}/${dvid}/rootFileNodes`
      : `${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/${DATASET_PUBLIC_VERSION_BASE_URL}/${dvid}/rootFileNodes`;
    return this.http.get<{ fileNodes: DatasetFileNode[]; size: number }>(apiUrl);
  }

  public deleteDatasets(dids: number[]): Observable<Response> {
    return this.http.post<Response>(`${AppSettings.getApiEndpoint()}/${DATASET_DELETE_URL}`, {
      dids: dids,
    });
  }

  public updateDatasetName(did: number, name: string): Observable<Response> {
    return this.http.post<Response>(`${AppSettings.getApiEndpoint()}/${DATASET_UPDATE_NAME_URL}`, {
      did: did,
      name: name,
    });
  }

  public updateDatasetDescription(did: number, description: string): Observable<Response> {
    return this.http.post<Response>(`${AppSettings.getApiEndpoint()}/${DATASET_UPDATE_DESCRIPTION_URL}`, {
      did: did,
      description: description,
    });
  }

  public updateDatasetPublicity(did: number): Observable<Response> {
    return this.http.post<Response>(
      `${AppSettings.getApiEndpoint()}/${DATASET_BASE_URL}/${did}/${DATASET_UPDATE_PUBLICITY_URL}`,
      {}
    );
  }

  public getDatasetOwners(did: number): Observable<number[]> {
    return this.http.get<number[]>(`${AppSettings.getApiEndpoint()}/${DATASET_GET_OWNERS_URL}?did=${did}`);
  }
}
