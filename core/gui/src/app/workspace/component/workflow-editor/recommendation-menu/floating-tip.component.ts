import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-floating-tip',
  template: `
    <div class="floating-tip-content">
      <p *ngFor="let tip of tips">{{ tip }}</p>
    </div>
  `,
  styleUrls: ['./floating-tip.component.scss']
})
export class FloatingTipComponent {
  @Input() tips: string[] = [];
}
