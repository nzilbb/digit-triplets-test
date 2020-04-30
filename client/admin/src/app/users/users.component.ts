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
    
    updateUser(user: User) {
        this.userService.updateUser(user)
            .subscribe(user => {
                // update the model with the user returned
                this.users.findIndex(u => { return u.user == user.user; });
                const i = this.users.findIndex(u => { return u.user == user.user; });
                if (i >= 0) this.users[i] = user;
            });
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
