import React, { Component } from 'react'
import './Sidebar.css'
import axios from 'axios';

import LibraryBooksIcon from '@material-ui/icons/LibraryBooks';
import PermMediaIcon from '@material-ui/icons/PermMedia';
import PublishIcon from '@material-ui/icons/Publish';
import SearchIcon from '@material-ui/icons/Search';
import { ProSidebar, Menu, MenuItem, SubMenu } from 'react-pro-sidebar';
import 'react-pro-sidebar/dist/css/styles.css'

export class Sidebar extends Component {
    constructor(props) {
        super(props);
        this.state = {
          setVisible: true,
          setClass: 'right',
          menu:'menu',
          setVisibleMenu:true,
          submenu:'',
          hierarchyCode:'1234-1',
            
         
        }
    
    this.display= this.display.bind(this);
    this.mediaLibrary= this.mediaLibrary.bind(this);
    }
        display(){
            
            if(this.state.setVisible)
            {
            this.setState(state=>({
                setVisible:false,
                setClass:'new',
                setVisibleMenu:true,
                menu:'menu',
                
             }
            ));
        }
        else if(!this.state.setVisible)
        {
            this.setState(state=>({
                setVisible:false,
                setClass:'right',
            }
            ));
        }
    }
   
    mediaLibrary(e){
        
        if(this.state.setVisibleMenu)
        {
        this.setState(state=>({
            setVisibleMenu:false,
            menu:'',
            setVisible:true,
            setClass:'right',
            
        }
        ));
        }
        else if(!this.state.setVisibleMenu)
        {
            this.props.displayMedia();
            this.setState(state=>({
                setVisibleMenu:true,
                menu:'menu',
                
            }))
        }
        axios({
          method: 'get',
          url: `http://mam-hackathon.com:8080/mediaHierarchy`
          })
          .then((response) => {
           response.data._embedded.mediaHierarchies.forEach((data, i) => {
              console.log('hierarchy name -'+data.hierarchyName);
            
            this.setState({
                data:response.data._embedded.mediaHierarchies
              })
           
           
           });
        });
      }
    render() {
        return (
            <div className="sidebar">
            <div className='left'>
            
              <div className="header__option" onClick={  this.display }>
                <SearchIcon fontSize="large" />
                <h3>Media Search</h3>
                </div>
                <div className="header__option"  onClick={(e) => this.mediaLibrary(e)} >
                <LibraryBooksIcon fontSize="large" />
                <h3>Library</h3>
                </div>
                <div className="header__option"  onClick= { this.props.displayMedia } >
                <PermMediaIcon fontSize="large" />
                <h3>Media</h3>
                </div>
                <div className="header__option" onClick= { this.props.action } >
                <PublishIcon fontSize="large"/>
                <h3>Upload</h3>
                </div>
                
               
                
             </div>
                <div className={ this.state.setClass }>
                <div className="header__input">
                 <SearchIcon />
                 <input placeholder="Media Search" type="text"   onChange={(e) => this.props.mediaSearch(e) } />
            
                 </div>
                    </div>

                         <div className={this.state.menu}>
                         <ProSidebar>
                         { this.state.data?.map((post, i) => (
                         <Menu key={post.hierarchyName} iconShape="square">
                        
                         <SubMenu title={post.hierarchyName} >
                         {post.children?.map((po,i) =>
                        <MenuItem key={po.hierarchyCode}  onClick= {(e) =>  this.props.mediaAction(po.hierarchyCode,e)}>{po.hierarchyName}</MenuItem>
                        )}
                        </SubMenu> 
                        </Menu >
                         ))}
                         <Menu>
                             <MenuItem key="Others"  onClick= {(e) =>  this.props.mediaAction(null,e)}>Others</MenuItem>
                         </Menu>
                        </ProSidebar>
                       </div>
                  </div>
       
        )
    }
}
export default Sidebar




