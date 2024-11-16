import { Component } from '@angular/core';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'recommendation-list',
  templateUrl: './recommendation.component.html',
})
export class RecommendationComponent {
  data = [
    { id: 1, name: 'Filter Operator' },
    { id: 2, name: 'Join Operator' },
    { id: 3, name: 'Projection Operator' },
    { id: 4, name: 'Aggregation Operator' },
  ];

  constructor(private msg: NzMessageService) {}

  onItemClick(item: any): void {
    this.msg.info(`You clicked on: ${item.name}`);
  }

  onApply(item: any, event: MouseEvent): void {
    event.stopPropagation(); // Prevent the list item click event
    this.msg.success(`Applied: ${item.name}`);
  }

  trackByFn(index: number, item: any): number {
    return item.id;
  }
}
