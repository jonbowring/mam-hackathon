import React from 'react'
import InfaLogo from './infa-logo.svg';
import './Header.css';
import FlagOutlinedIcon from '@material-ui/icons/FlagOutlined';
import HelpOutlineOutlinedIcon from '@material-ui/icons/HelpOutlineOutlined';
import NotificationsActiveIcon from '@material-ui/icons/NotificationsActive';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import {  IconButton } from '@material-ui/core';
function Header() {
   

    return (
      
            <div className="app-header">
                <div className='header__left'>
                 <img className="header__logo" src={ InfaLogo } alt="Informatica"/>
          </div>
          
          
                
          
          <div className="header__right">
         
              <IconButton>
                  <FlagOutlinedIcon className="icons"/>
              </IconButton>
              <IconButton>
                  <HelpOutlineOutlinedIcon className="icons"/>
              </IconButton>
              <IconButton>
                  <NotificationsActiveIcon className="icons"/>
              </IconButton>
              <IconButton>
                  <ExpandMoreIcon className="icons"/>
              </IconButton>
              </div>  
        </div>
    
      
    )
}

export default Header
