import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { TextComponent } from './text/text.component';
import { SoundCheckComponent } from './sound-check/sound-check.component';

const routes: Routes = [
    { path: '', redirectTo: 'text/introduction', pathMatch: 'full' },
    { path: 'text/:id', component: TextComponent },
    { path: 'sound-check', component: SoundCheckComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
