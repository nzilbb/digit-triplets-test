import { Component, OnInit } from '@angular/core';

import { Instance } from '../instance';
import { InstanceService } from '../instance.service';
import { Field } from '../field';
import { FieldService } from '../field.service';

@Component({
  selector: 'app-instances',
  templateUrl: './instances.component.html',
  styleUrls: ['./instances.component.css']
})
export class InstancesComponent implements OnInit {
    instances: Instance[];
    fields: Field[];
    page = 0;
    pageLength = 20;
    noMorePages = false;

    constructor(
        private instanceService: InstanceService,
        private fieldService: FieldService
    ) { }
    
    ngOnInit(): void {
        this.readFields();    
        this.readInstances();    
    }

    readInstances(): void {
        this.instanceService.readInstances(this.page, this.pageLength)
            .subscribe((instances) => {
                this.noMorePages = instances.length < this.pageLength;
                if (instances.length > 0) {
                    this.instances = instances;
                }
            });
    }
    readFields(): void {
        this.fieldService.readFields()
            .subscribe(fields => this.fields = fields);
    }

    getFieldValue(instance_id: string, field: string): string {
        return "something";
    }

    pageDown() {
        this.page++;
        this.readInstances();
    }
    pageUp() {
        this.page--;
        this.readInstances();
    }

    csv() {
        this.instanceService.csv();
    }

}
