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
    changed = false;
    
    constructor(
        private userService: UserService
    ) {}
    
    ngOnInit(): void {
        this.readUsers();
    }

    createUser(user: string, email: string, reset_password: boolean, password: string) {
        this.userService.createUser({ user, email, reset_password, password } as User)
            .subscribe(user => {
                // add to the model/view
                if (user) this.users.push(user); });
    }
    
    readUsers(): void {
        this.userService.readUsers()
            .subscribe(users => this.users = users);
    }

    updateUser(user: User) {
        this.userService.updateUser(user)
            .subscribe(user => {
                // update the model with the user returned
                const i = this.users.findIndex(u => { return u.user == user.user; });
                if (i >= 0) this.users[i] = user;
                this.updateChangedFlag();
            });
    }
    
    deleteUser(user: User) {
        this.userService.deleteUser(user)
            .subscribe(returnedUser => {
                // when removal is successful, returnedUser == null
                if (!returnedUser) {
                    // remove from the model/view
                    this.users = this.users.filter(u => { return u !== user;});
                    this.updateChangedFlag();
                }});
    }
    
    onChange(user: User) {
        user.changed = this.changed = true;        
    }

    updateChangedUsers() {
        this.users
            .filter(u => u.changed)
            .forEach(u => this.updateUser(u));
    }

    updateChangedFlag() {
        this.changed = false;
        for (let user of this.users) {
            if (user.changed) {
                this.changed = true;
                break; // only need to find one
            }
        } // next user
    }
}
