import {Component} from "@angular/core";

@Component({
    selector: 'texera-user-environment-datasets-viewer',
    templateUrl: './user-environment-datasets-viewer.component.html',
    styleUrls: ['./user-environment-datasets-viewer.component.scss']
})
export class UserEnvironmentDatasetsViewerComponent {
    panels = [
        { title: 'Header 1', content: 'Content 1' },
        { title: 'Header 2', content: 'Content 2' },
        { title: 'Header 3', content: 'Content 3' }
    ];

    onMoreAction(index: number, event: MouseEvent): void {
        event.stopPropagation(); // Prevent the collapse panel from toggling
        console.log('More action clicked on panel index:', index);
        // Implement your more action logic here
    }
}
