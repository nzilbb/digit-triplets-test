import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';

import { Message } from '../message';
import { UserService } from '../user.service';

@Component({
  selector: 'app-password',
  templateUrl: './password.component.html',
  styleUrls: ['./password.component.css']
})
export class PasswordComponent implements OnInit {

    password = "";
    repeatPassword = "";
    canSave = false;
    
    constructor(
        private userService: UserService,
        private location: Location
    ) { }
    
    ngOnInit(): void {
    }

    checkForm(): void {
        this.canSave = this.password.length > 0
            && this.password == this.repeatPassword;
    }

    setPassword(): void {
        if (this.canSave) {
            this.userService.setPassword(this.password)
                .subscribe((message)=>{
                    this.location.back();
                });
            
        }
    }
    
}
