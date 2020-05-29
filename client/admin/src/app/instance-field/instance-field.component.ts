import { Component, OnInit, Input } from '@angular/core';

import { InstanceFieldService } from '../instance-field.service';
import { InstanceField } from '../instancefield';

@Component({
    selector: 'app-instance-field',
    templateUrl: './instance-field.component.html',
    styleUrls: ['./instance-field.component.css']
})
export class InstanceFieldComponent implements OnInit {
    @Input() instance_id: string;
    @Input() field: string;
    value: string;
    
    constructor(private instanceFieldService: InstanceFieldService) { }
    
    ngOnInit(): void {
        this.getValue();
    }
    getValue(): void {
        this.instanceFieldService.getFieldValue(this.instance_id, this.field)
            .subscribe((field) => {
                this.value = field.value;
            });
    }
    
}
