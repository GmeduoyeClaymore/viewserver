import Logger from 'common/Logger';
import { isEqual } from '../../is-equal';
import invariant from 'invariant';
const MaxStackLength = 2;
export default class NavigationTransformation{
  constructor(navContainer){
    invariant(navContainer, 'navContainer must be defined');
    this.navContainer = navContainer;
    this.goBack = this.goBack.bind(this);
    this.next = this.next.bind(this);
    this.replace = this.replace.bind(this);
  }
  
  get location(){
    const navPointer = this.navContainer.navPointer;
    return  this.navContainer.navigationStack[navPointer];
  }
  
  get navigationStack(){
    const navPointer = this.navContainer.navPointer;
    return  this.navContainer.navigationStack.slice(0, navPointer + 1);
  }
  
  get version(){
    return this.navContainer.version;
  }

  get navPointer(){
    return this.navContainer.navPointer;
  }
    
  goBack(){
    const newNavPointer = this.navContainer.navPointer - 1;
    const newStack = this.navContainer.navigationStack.slice(0, newNavPointer + MaxStackLength);
    return this.incrementVersionCounter().setIn(['navPointer'], !~newNavPointer ? 0 : newNavPointer ).setIn(['navigationStack'], newStack);
  }
    
  next(action){
    const newNavPointer = this.navContainer.navPointer + 1;
    const newStack = [...this.navigationStack];
    newStack[newNavPointer] = action;
    Logger.info(`After next new stack  from ${JSON.stringify(this.navContainer)} to ${JSON.stringify(newStack)}`);
    return this.incrementVersionCounter().setIn(['navPointer'], newNavPointer).setIn(['navigationStack'], newStack);
  }
  
  just(action){
    const newStack = [action];
    return this.incrementVersionCounter().setIn(['navPointer'], 0).setIn(['navigationStack'], newStack);
  }
    
  replace(action){
    const newStack = [...this.navigationStack];
    newStack[this.navContainer.navPointer] = action;
    return this.incrementVersionCounter().setIn(['navigationStack'], newStack);
  }
  
  incrementVersionCounter(){
    const existingVersion = this.navContainer.version || 0;
    return this.navContainer.setIn(['version'], existingVersion + 1);
  }
}
