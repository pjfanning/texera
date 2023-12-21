import {Component, Input, OnInit} from '@angular/core';
import {UntilDestroy} from "@ngneat/until-destroy";

@UntilDestroy()
@Component({
  selector: 'texera-environment-dataset-item',
  templateUrl: './environment-dataset-item.component.html',
  styleUrls: ['./environment-dataset-item.component.scss']
})
export class EnvironmentDatasetItemComponent implements OnInit{
  @Input() dataset: any = [];

  isExpanded = false;

  toggleExpand(): void {
    this.isExpanded = !this.isExpanded;
  }

  constructor() {
  }
  ngOnInit(): void {
  }
}
