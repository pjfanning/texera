import { ChangeDetectionStrategy, Component } from '@angular/core';
import { moveItemInArray } from '@angular/cdk/drag-drop';
import { FormlyFieldSelect } from '@ngx-formly/material/select';

@Component({
  template: `
    <ng-template #selectAll let-selectOptions="selectOptions">
      <mat-option (click)="toggleSelectAll(selectOptions)">
        <mat-pseudo-checkbox class="mat-option-pseudo-checkbox" [state]="getSelectAllState(selectOptions)">
        </mat-pseudo-checkbox>
        {{ to.selectAllOption }}
      </mat-option>
    </ng-template>
    <mat-select
      [id]="id"
      [formControl]="formControl"
      [formlyAttributes]="field"
      [placeholder]="to.placeholder"
      [tabIndex]="to.tabindex"
      [required]="to.required"
      [compareWith]="to.compareWith"
      [multiple]="to.multiple"
      (selectionChange)="change($event)"
      [errorStateMatcher]="errorStateMatcher"
      [aria-labelledby]="_getAriaLabelledby()"
      [disableOptionCentering]="to.disableOptionCentering"
    >
      <ng-container *ngIf="to.options | formlySelectOptions: field | async as selectOptions">
        <ng-container
          *ngIf="to.multiple && to.selectAllOption"
          [ngTemplateOutlet]="selectAll"
          [ngTemplateOutletContext]="{ selectOptions: selectOptions }"
        >
        </ng-container>
        <div class="formly-drag-drop" cdkDropList
             (cdkDropListDropped)="moveItemInArray(selectOptions, $event.previousIndex, $event.currentIndex);">
          <span *ngFor="let item of selectOptions" cdkDrag>
            <mat-option style="position: relative;" *ngIf="!item.group" [value]="item.value" [disabled]="item.disabled">{{ item.label }}
              <i nz-icon nzType="menu" nzTheme="outline" cdkDragHandle
                 style="right: 10px;position: absolute;top: 50%;transform: translateY(-50%);"></i>
            </mat-option>
          </span>
        </div>
      </ng-container>
    </mat-select>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DraggableArrayTypeComponent extends FormlyFieldSelect {
  moveItemInArray = moveItemInArray;
}
