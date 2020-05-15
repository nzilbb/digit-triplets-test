import { Component, OnInit } from '@angular/core';

import { Instance } from '../instance';
import { InstanceService } from '../instance.service';

@Component({
  selector: 'app-instances',
  templateUrl: './instances.component.html',
  styleUrls: ['./instances.component.css']
})
export class InstancesComponent implements OnInit {
    instances: Instance[];
    page = 0;
    pageLength = 20;
    noMorePages = false;

    constructor(
        private instanceService: InstanceService
    ) { }
    
    ngOnInit(): void {
        this.readInstances();    
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

    readInstances(): void {
        this.instanceService.readInstances(this.page, this.pageLength)
            .subscribe((instances) => {
                this.noMorePages = instances.length < this.pageLength;
                if (instances.length > 0) {
                    this.instances = instances;
                }
            });
    }
}
