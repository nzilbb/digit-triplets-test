import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { User } from '../user';
import { UserService } from '../user.service';
import { MessageService } from '../message.service';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit {

    user: User;
    
    constructor(
        private userService: UserService,
        private messageService: MessageService,
        private router: Router
    ) { }
    
    ngOnInit(): void {
        this.userService.whoAmI()
            .subscribe((user) => {
                this.user = user;
                if (this.user.reset_password) {
                    this.messageService.info("Please change your password");
                    this.router.navigateByUrl("/password");
                }
            });
    }

}
