import { Component, OnInit, Input } from '@angular/core';
import { User } from '../user';
import { UserService } from '../user.service';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit {
    users: User[];
    
    constructor(
        private userService: UserService
    ) {}
    
    ngOnInit(): void {
        this.getUsers();
    }

    getUsers(): void {
        this.userService.getUsers()
            .subscribe(users => this.users = users);
    }

    addUser(user: string, email: string, reset_password: boolean, password: string) {
        this.userService.addUser({ user, email, reset_password, password } as User)
            .subscribe(user => {
                // add to the model/view
                if (user) this.users.push(user); });
    }
    
    removeUser(user: User) {
        this.userService.removeUser(user)
            .subscribe(returnedUser => {
                // when removal is successful, returnedUser == null
                if (!returnedUser) {
                    // remove from the model/view
                    this.users = this.users.filter(u => { return u !== user;});
                }});
    }
}
