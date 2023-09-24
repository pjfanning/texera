import { AfterViewInit, Component, Input, OnInit } from "@angular/core";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { NgbActiveModal, NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { AdminUserService } from "../../../admin/service/admin-user.service";
import { File, Workflow, mongoStorage } from "../../../../common/type/user"

import * as c3 from "c3";
import { ChartType } from "src/app/workspace/types/visualization.interface";


@UntilDestroy()
@Component({
  templateUrl: './ngbd-modal-user-quota.component.html',
  styleUrls: ['./ngbd-modal-user-quota.component.scss']
})

export class NgbModalUserQuotaComponent implements OnInit, AfterViewInit{
  public static readonly MONGODB_PIE_CHART = "#mongodb-storage-pie-chart";
  public static readonly MONGODB_BAR_CHART = "#uploaded-file-bar-chart";

  public static readonly WIDTH = 300;
  public static readonly HEIGHT = 300;
  public static readonly BARCHARTSIZE = 300;

  userUid: number = 0;
  totalSize: string = "";
  createdFiles: ReadonlyArray<File> = [];
  createdWorkflows: ReadonlyArray<Workflow> = [];
  accessFiles: ReadonlyArray<number> = [];
  accessWorkflows: ReadonlyArray<number> = [];
  topFiveFiles: ReadonlyArray<File> = [];
  mongodbStorages: ReadonlyArray<mongoStorage> = [];

  constructor(private adminUserService: AdminUserService) {
  }

  ngOnInit(): void {
    this.adminUserService
      .getUploadedFiles(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(fileList => {
        this.createdFiles = fileList;
        let size = 0;
        this.createdFiles.forEach(file => size += file.file_size / 1000000)
        this.totalSize = size.toPrecision(2);
      });
    
    this.adminUserService
      .getCreatedWorkflows(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(workflowList => {
        this.createdWorkflows = workflowList;
      });
    
    this.adminUserService
      .getAccessFiles(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(accessFiles => {
        this.accessFiles = accessFiles;
      });

    this.adminUserService
      .getAccessWorkflows(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(accessWorkflows => {
        this.accessWorkflows = accessWorkflows;
      });
  
    }

    ngAfterViewInit(): void {
      this.adminUserService
      .getMongoDBs(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(result => {
        this.mongodbStorages = result;
        let mongoWorkflowData: { [key: string]: [string, number] } = {};
        let totalSize: number = 0;

        this.mongodbStorages.forEach(mongo => {
          if (mongoWorkflowData[mongo.workflowName] === undefined) {
            mongoWorkflowData[mongo.workflowName] = [mongo.workflowName, mongo.size];
          } else {
            mongoWorkflowData[mongo.workflowName][1] += mongo.size
          }

          totalSize += mongo.size;
        })
        
        this.generatePieChart(
          Object.values(mongoWorkflowData),
          ["workflow"], 
          "MongoDB Total Storage Usage: " + totalSize + " (kb)",
          NgbModalUserQuotaComponent.MONGODB_PIE_CHART
        );
      });

      this.adminUserService
      .getUploadedFiles(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(fileList => {
        const copiedFiles = [...fileList];
        copiedFiles.sort((a, b) => b.file_size - a.file_size);
        this.topFiveFiles = copiedFiles.slice(0, 5);

        let fileSizes: Array<[string, ...c3.PrimitiveArray]> = [["file sizes"]];
        let fileNames: string[] = [];
        this.topFiveFiles.forEach(file => {
          fileSizes[0].push(file.file_size / 1000000);
          fileNames.push(file.file_name)
        })
        console.log(fileSizes);
        console.log(fileNames);

        this.generateBarChart(
          fileSizes,
          fileNames,
          "File Names",
          "File Sizes (mb)",
          "Top 5 File Sizes",
          NgbModalUserQuotaComponent.MONGODB_BAR_CHART
        );
      });
    }

    generatePieChart(
      dataToDisplay: Array<[string, ...c3.PrimitiveArray]>,
      category: string[],
      title: string,
      chart: string
    ) {
      c3.generate({
        size: {
          height: NgbModalUserQuotaComponent.HEIGHT,
          width: NgbModalUserQuotaComponent.WIDTH,
        },
        data: {
          columns: dataToDisplay,
          type: ChartType.PIE,
        },
        axis: {
          x: {
            type: "category",
            categories: category,
          },
        },
        title: {
          text: title,
        },
        bindto: chart,
      });
    }

    generateBarChart(
      dataToDisplay: Array<[string, ...c3.PrimitiveArray]>,
      category: string[],
      x_label: string,
      y_label: string,
      title: string,
      chart: string
    ) {
      c3.generate({
        size: {
          height: NgbModalUserQuotaComponent.BARCHARTSIZE,
          width: NgbModalUserQuotaComponent.BARCHARTSIZE,
        },
        data: {
          columns: dataToDisplay,
          type: ChartType.BAR,
        },
        axis: {
          x: {
            label: {
              text: x_label,
              position: "outer-right",
            },
            type: "category",
            categories: category,
          },
          y: {
            label: {
              text: y_label,
              position: "outer-top",
            },
          },
        },
        title: {
          text: title,
        },
        bindto: chart,
      });
    }
}